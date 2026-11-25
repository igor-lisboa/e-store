package br.uff.store.core.services;

import java.math.BigDecimal;
import java.util.List;

import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.dtos.CartItemDTO;
import br.uff.store.core.dtos.DeliveryResultDTO;
import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.dtos.PaymentResultDTO;
import br.uff.store.core.dtos.SaleDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.ISaleData;
import br.uff.store.core.interfaces.gateways.IDeliveryGateway;
import br.uff.store.core.interfaces.gateways.IPaymentGateway;
import br.uff.store.core.interfaces.services.ICartService;
import br.uff.store.core.interfaces.services.IAddressService;
import br.uff.store.core.interfaces.services.ISaleService;

public class SaleService implements ISaleService {

    private final ISaleData saleData;
    private final ICartService cartService;
    private final IAddressService addressService;
    private final IPaymentGateway paymentGateway;
    private final IDeliveryGateway deliveryGateway;

    public SaleService(ISaleData saleData, ICartService cartService, IAddressService addressService,
            IPaymentGateway paymentGateway, IDeliveryGateway deliveryGateway) {
        this.saleData = java.util.Objects.requireNonNull(saleData, "Sale data is required.");
        this.cartService = java.util.Objects.requireNonNull(cartService, "Cart service is required.");
        this.addressService = java.util.Objects.requireNonNull(addressService, "Address service is required.");
        this.paymentGateway = java.util.Objects.requireNonNull(paymentGateway, "Payment gateway is required.");
        this.deliveryGateway = java.util.Objects.requireNonNull(deliveryGateway, "Delivery gateway is required.");
    }

    @Override
    public List<SaleDTO> listUserSales(Integer userId) throws StoreException {
        return saleData.listUserSales(userId);
    }

    @Override
    public void recordSale(Integer userId, Integer cartId, Integer addressId) throws StoreException {
        InputRules.positiveId(userId, "User ID");
        InputRules.positiveId(cartId, "Cart ID");
        InputRules.positiveId(addressId, "Address ID");
        if (!Boolean.TRUE.equals(cartService.isValidActiveCart(cartId, userId))) {
            throw new StoreException("Cart ID " + cartId + " is invalid, empty, sold, or owned by another user.");
        }
        if (!Boolean.TRUE.equals(addressService.addressBelongsToUser(addressId, userId))) {
            throw new StoreException("Address ID " + addressId + " does not belong to cart owner ID " + userId + ". Choose another address.");
        }
        AddressDTO destination = addressService.findAddressById(addressId);
        PaymentResultDTO payment = paymentGateway.authorize(userId, cartTotal(cartId));
        try {
            DeliveryResultDTO delivery = deliveryGateway.arrange(userId, destination);
            saleData.recordSale(cartId, addressId, payment, delivery);
        } catch (StoreException | RuntimeException failure) {
            releaseQuietly(payment, failure);
            throw failure;
        }
    }

    @Override
    public PaginationDTO<List<SaleDTO>> listUserSales(Integer userId, Integer pageSize, Integer pageNumber) throws StoreException {
        InputRules.page(pageSize, pageNumber);
        PaginationDTO<List<SaleDTO>> sales = saleData.listUserSales(userId, pageSize, pageNumber);
        for (SaleDTO sale : sales.getItems()) {
            sale.setLines(saleData.listSaleLines(sale.getId()));
        }
        return sales;
    }

    @Override
    public void cancelSale(Integer userId, Integer saleId) throws StoreException {
        InputRules.positiveId(userId, "User ID");
        InputRules.positiveId(saleId, "Order ID");
        saleData.cancelSale(saleId, userId);
    }

    private BigDecimal cartTotal(Integer cartId) throws StoreException {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItemDTO item : cartService.listCartProducts(cartId)) {
            total = total.add(item.getTotalPrice());
        }
        return total;
    }

    private void releaseQuietly(PaymentResultDTO payment, Exception original) {
        try {
            paymentGateway.release(payment);
        } catch (StoreException | RuntimeException releaseFailure) {
            original.addSuppressed(releaseFailure);
        }
    }
}

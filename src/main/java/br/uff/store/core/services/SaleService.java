package br.uff.store.core.services;

import java.util.List;

import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.dtos.SaleDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.ISaleData;
import br.uff.store.core.interfaces.services.ICartService;
import br.uff.store.core.interfaces.services.IAddressService;
import br.uff.store.core.interfaces.services.ISaleService;

public class SaleService implements ISaleService {

    private final ISaleData saleData;
    private final ICartService cartService;
    private final IAddressService addressService;

    public SaleService(ISaleData saleData, ICartService cartService, IAddressService addressService) {
        this.saleData = java.util.Objects.requireNonNull(saleData, "Sale data is required.");
        this.cartService = java.util.Objects.requireNonNull(cartService, "Cart service is required.");
        this.addressService = java.util.Objects.requireNonNull(addressService, "Address service is required.");
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
        saleData.recordSale(cartId, addressId);
    }

    @Override
    public PaginationDTO<List<SaleDTO>> listUserSales(Integer userId, Integer pageSize, Integer pageNumber) throws StoreException {
        InputRules.page(pageSize, pageNumber);
        return saleData.listUserSales(userId, pageSize, pageNumber);
    }
}

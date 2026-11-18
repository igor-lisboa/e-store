package br.uff.store;

import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.dtos.SaleDTO;
import br.uff.store.core.exceptions.StoreException;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class LegacyStoreIntegrationCheckoutTest extends StoreIntegrationFixture {
    @Test
    public void checkoutRecordsOwnerAddressItemsTotalAndStockChange() throws Exception {
        AddressDTO address = createAddress(2);
        CartDTO cart = createCart(2, 1, 2);
        sales.recordSale(2, cart.getId(), address.getId());
        List<SaleDTO> orders = sales.listUserSales(2);
        assertEquals(1, orders.size());
        SaleDTO order = orders.get(0);
        assertEquals(cart.getId(), order.getCartId());
        assertEquals(address.getId(), order.getAddressId());
        assertEquals(Integer.valueOf(2), order.getUserId());
        assertEquals(998.00, order.getTotalPrice(), 0.001);
        assertEquals(Integer.valueOf(98), products.findProductById(1).getQuantity());
        assertEquals(Integer.valueOf(2), carts.listCartProducts(order.getCartId()).get(0).getQuantity());
    }

    @Test
    public void anotherAccountCannotBuyTheCart() throws Exception {
        AddressDTO address = createAddress(1);
        CartDTO cart = createCart(2, 1, 1);
        assertThrows(StoreException.class, () -> sales.recordSale(1, cart.getId(), address.getId()));
        assertTrue(sales.listUserSales(1).isEmpty());
        assertTrue(sales.listUserSales(2).isEmpty());
        assertEquals(Integer.valueOf(100), products.findProductById(1).getQuantity());
    }

    @Test
    public void anotherAccountsAddressCannotBeUsedForCheckout() throws Exception {
        AddressDTO address = createAddress(1);
        CartDTO cart = createCart(2, 1, 1);
        StoreException failure = assertThrows(StoreException.class,
                () -> sales.recordSale(2, cart.getId(), address.getId()));
        assertTrue(failure.getMessage().contains("does not belong to cart owner"));
        assertTrue(sales.listUserSales(2).isEmpty());
        assertEquals(Integer.valueOf(100), products.findProductById(1).getQuantity());
    }

    @Test
    public void soldCartCannotBeMutatedOrPurchasedAgain() throws Exception {
        AddressDTO address = createAddress(2);
        CartDTO cart = createCart(2, 1, 1);
        sales.recordSale(2, cart.getId(), address.getId());
        assertThrows(StoreException.class, () -> carts.changeProductQuantity(cart.getId(), 1, 3));
        assertThrows(StoreException.class, () -> sales.recordSale(2, cart.getId(), address.getId()));
        assertEquals(Integer.valueOf(1), carts.productQuantityInCart(cart.getId(), 1));
        assertEquals(Integer.valueOf(99), products.findProductById(1).getQuantity());
        assertEquals(1, sales.listUserSales(2).size());
    }

    @Test
    public void recoveringSoldCartCreatesANewActiveCart() throws Exception {
        AddressDTO address = createAddress(2);
        CartDTO sold = createCart(2, 1, 1);
        sales.recordSale(2, sold.getId(), address.getId());
        CartDTO active = carts.getActiveCart(sold.getId(), 2, "127.0.0.1");
        assertNotEquals(sold.getId(), active.getId());
        assertEquals(Integer.valueOf(2), active.getUserId());
        assertTrue(carts.listCartProducts(active.getId()).isEmpty());
        assertEquals(sold.getId(), sales.listUserSales(2).get(0).getCartId());
    }

    @Test
    public void orderHistoryPaginationReturnsDistinctOrdersOnlyForTheirOwner() throws Exception {
        AddressDTO address = createAddress(2);
        CartDTO first = createCart(2, 1, 1);
        sales.recordSale(2, first.getId(), address.getId());
        CartDTO second = createCart(2, 2, 1);
        sales.recordSale(2, second.getId(), address.getId());
        PaginationDTO<List<SaleDTO>> newest = sales.listUserSales(2, 1, 1);
        PaginationDTO<List<SaleDTO>> older = sales.listUserSales(2, 1, 2);
        assertEquals(Integer.valueOf(2), newest.getLastPage());
        assertEquals(1, newest.getItems().size());
        assertEquals(1, older.getItems().size());
        assertEquals(second.getId(), newest.getItems().get(0).getCartId());
        assertEquals(first.getId(), older.getItems().get(0).getCartId());
        assertTrue(sales.listUserSales(1, 1, 1).getItems().isEmpty());
    }
}

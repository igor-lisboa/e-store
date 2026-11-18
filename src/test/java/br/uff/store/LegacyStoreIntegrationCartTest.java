package br.uff.store;

import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.dtos.CartItemDTO;
import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.exceptions.StoreException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

public class LegacyStoreIntegrationCartTest extends StoreIntegrationFixture {
    @Test
    public void addingAnExistingItemIncreasesQuantityWithoutDuplicateRows() throws Exception {
        CartDTO cart = createCart(2, 1, 1);
        carts.addProductToCart(cart.getId(), 1);
        List<CartItemDTO> items = carts.listCartProducts(cart.getId());
        assertEquals(1, items.size());
        assertEquals(Integer.valueOf(2), items.get(0).getQuantity());
        assertEquals(998.00, carts.getCartTotal(cart.getId()), 0.001);
        assertEquals(Integer.valueOf(100), products.findProductById(1).getQuantity());
    }

    @Test
    public void removingOneItemPreservesOtherItemsAndTheirTotal() throws Exception {
        CartDTO cart = createCart(2, 1, 1);
        carts.addProductToCart(cart.getId(), 2);
        carts.removeProductFromCart(cart.getId(), 1);
        List<CartItemDTO> items = carts.listCartProducts(cart.getId());
        assertEquals(1, items.size());
        assertEquals(Integer.valueOf(2), items.get(0).getProductId());
        assertEquals(399.00, carts.getCartTotal(cart.getId()), 0.001);
    }

    @Test
    public void unavailableQuantityDoesNotChangeTheCart() throws Exception {
        CartDTO cart = createCart(2, 1, 2);
        StoreException failure = assertThrows(StoreException.class,
                () -> carts.changeProductQuantity(cart.getId(), 1, 101));
        assertEquals("Requested quantity exceeds available stock.", failure.getMessage());
        assertEquals(Integer.valueOf(2), carts.productQuantityInCart(cart.getId(), 1));
        assertEquals(998.00, carts.getCartTotal(cart.getId()), 0.001);
    }

    @Test
    public void anonymousCartCanBeRecoveredAndClaimedWithItsItems() throws Exception {
        CartDTO anonymous = carts.getActiveCart(null, null, "127.0.0.1");
        carts.addProductToCart(anonymous.getId(), 1);
        assertEquals(anonymous.getId(), carts.getActiveCart(anonymous.getId(), null, "127.0.0.1").getId());
        CartDTO claimed = carts.getActiveCart(anonymous.getId(), 2, "127.0.0.1");
        assertEquals(anonymous.getId(), claimed.getId());
        assertEquals(Integer.valueOf(2), claimed.getUserId());
        assertEquals(Integer.valueOf(1), carts.listCartProducts(claimed.getId()).get(0).getProductId());
    }

    @Test
    public void anotherAccountCannotRecoverAnOwnedCart() throws Exception {
        CartDTO owned = createCart(2, 1, 1);
        CartDTO other = carts.getActiveCart(owned.getId(), 1, "127.0.0.1");
        assertNotEquals(owned.getId(), other.getId());
        assertEquals(Integer.valueOf(1), other.getUserId());
        assertTrue(carts.listCartProducts(other.getId()).isEmpty());
        assertEquals(Integer.valueOf(1), carts.productQuantityInCart(owned.getId(), 1));
    }

    @Test
    public void paginationVisitsEachCartItemExactlyOnce() throws Exception {
        CartDTO cart = createCart(2, 1, 1);
        carts.addProductToCart(cart.getId(), 2);
        carts.addProductToCart(cart.getId(), 3);
        List<Integer> visited = new ArrayList<>();
        for (int page = 1; page <= 3; page++) {
            PaginationDTO<List<CartItemDTO>> result = carts.listCartProducts(cart.getId(), 1, page);
            assertEquals(Integer.valueOf(3), result.getLastPage());
            assertEquals(1, result.getItems().size());
            visited.add(result.getItems().get(0).getProductId());
        }
        assertEquals(Arrays.asList(1, 2, 3), visited.stream().sorted().collect(Collectors.toList()));
        assertEquals(1097.00, carts.getCartTotal(cart.getId()), 0.001);
    }
}

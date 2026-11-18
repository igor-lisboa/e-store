package br.uff.store;

import br.uff.store.application.StoreServices;
import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.interfaces.services.IAddressService;
import br.uff.store.core.interfaces.services.ICartService;
import br.uff.store.core.interfaces.services.IProductService;
import br.uff.store.core.interfaces.services.IReviewService;
import br.uff.store.core.interfaces.services.ISaleService;
import br.uff.store.core.interfaces.services.IUserService;
import br.uff.store.infrastructure.database.IsolatedMySqlTest;

/** Every case inherits a guarded schema reset; fixture IDs are the versioned dev seeds. */
public abstract class StoreIntegrationFixture extends IsolatedMySqlTest {
    protected final IProductService products = StoreServices.products();
    protected final IUserService users = StoreServices.users();
    protected final IAddressService addresses = StoreServices.addresses();
    protected final ICartService carts = StoreServices.carts();
    protected final ISaleService sales = StoreServices.sales();
    protected final IReviewService reviews = StoreServices.reviews();

    protected ProductDTO createProduct(String name, double price, int stock) throws Exception {
        products.insertProduct(new ProductDTO(null, name, price, "Integration fixture", "img/ps4.jpg", 5, stock));
        ProductDTO created = products.listAdminProducts().stream().filter(product -> name.equals(product.getName()))
                .findFirst().orElseThrow(() -> new AssertionError("Created product was not persisted."));
        if (stock > 0) {
            products.addProductStock(created.getId(), stock);
        }
        return products.findProductById(created.getId());
    }

    protected AddressDTO createAddress(int userId) throws Exception {
        addresses.insertAddress(new AddressDTO("Test home", userId, 12345, "12 Main Street",
                "Test City", "Test State", "Test Country"));
        return addresses.listUserAddresses(userId).get(0);
    }

    protected CartDTO createCart(int userId, int productId, int quantity) throws Exception {
        CartDTO cart = carts.getActiveCart(null, userId, "127.0.0.1");
        carts.addProductToCart(cart.getId(), productId);
        if (quantity != 1) {
            carts.changeProductQuantity(cart.getId(), productId, quantity);
        }
        return cart;
    }
}

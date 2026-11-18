package br.uff.store.application;

import br.uff.store.core.services.ProductService;
import br.uff.store.core.services.UserService;
import br.uff.store.core.services.AddressService;
import br.uff.store.core.services.CartService;
import br.uff.store.core.services.CategoryService;
import br.uff.store.core.services.RoleService;
import br.uff.store.core.services.ReviewService;
import br.uff.store.core.services.SaleService;
import br.uff.store.infrastructure.data.ProductData;
import br.uff.store.infrastructure.data.UserData;
import br.uff.store.infrastructure.data.AddressData;
import br.uff.store.infrastructure.data.CartData;
import br.uff.store.infrastructure.data.CategoryData;
import br.uff.store.infrastructure.data.RoleData;
import br.uff.store.infrastructure.data.ProductReviewData;
import br.uff.store.infrastructure.data.SaleData;
import br.uff.store.infrastructure.security.Pbkdf2PasswordHasher;

/** The application boundary wires concrete adapters into framework-independent use cases. */
public final class StoreServices {
    private StoreServices() { }

    public static ProductService products() {
        return new ProductService(new ProductData());
    }

    public static UserService users() {
        return new UserService(new UserData(), new Pbkdf2PasswordHasher());
    }

    public static AddressService addresses() {
        return new AddressService(new AddressData());
    }

    public static CartService carts() {
        return new CartService(new CartData(), new ProductData());
    }

    public static CategoryService categories() {
        return new CategoryService(new CategoryData());
    }

    public static RoleService roles() {
        return new RoleService(new RoleData());
    }

    public static ReviewService reviews() {
        return new ReviewService(new ProductReviewData());
    }

    public static SaleService sales() {
        return new SaleService(new SaleData(), carts(), addresses());
    }
}

package br.uff.store.core.services;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.dtos.CartItemDTO;
import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.ICartData;
import br.uff.store.core.interfaces.data.IProductData;
import br.uff.store.core.interfaces.services.ICartService;

public class CartService implements ICartService {

    private final ICartData cartData;
    private final IProductData productData;
    private final Clock clock;
    private static final String CART_ID_CONTEXT = " in cart ID ";

    public CartService(ICartData cartData, IProductData productData) {
        this(cartData, productData, Clock.systemUTC());
    }

    public CartService(ICartData cartData, IProductData productData, Clock clock) {
        if (cartData == null || productData == null || clock == null) {
            throw new IllegalArgumentException("Cart, product, and clock dependencies are required.");
        }
        this.cartData = cartData;
        this.productData = productData;
        this.clock = clock;
    }

    @Override
    public CartDTO getActiveCart(Integer cartId, Integer userId, String ip) throws StoreException {
        if (cartId != null) {
            if (Boolean.TRUE.equals(cartData.cartExists(cartId)) && Boolean.FALSE.equals(cartData.isCartSold(cartId))) {
                if (userId == null) {
                    if (Boolean.TRUE.equals(cartData.isCartUnowned(cartId))) {
                        return cartData.findCart(cartId);
                    }
                } else {
                    if (Boolean.TRUE.equals(cartData.isCartUnowned(cartId))) {
                        cartData.assignUnownedCart(cartId, userId);
                    }
                    if (Boolean.TRUE.equals(cartData.cartBelongsToUser(cartId, userId))) {
                        return cartData.findCart(cartId);
                    }
                }
            }
        }
        if (userId != null) {
            List<CartDTO> unsoldCarts = cartData.listUnsoldUserCarts(userId);
            if (Boolean.FALSE.equals(unsoldCarts.isEmpty())) {
                return unsoldCarts.get(0);
            }
        }
        String createdAt = LocalDateTime.now(clock).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        cartData.createCart(ip, createdAt, userId);
        return cartData.findCart(ip, createdAt, userId);
    }

    @Override
    public void addProductToCart(Integer cartId, Integer productId) throws StoreException {
        if (Boolean.TRUE.equals(cartData.cartExists(cartId)) && Boolean.FALSE.equals(cartData.isCartSold(cartId))) {
            Integer storedQuantity = cartData.productQuantityInCart(cartId, productId);
            int currentQuantity = storedQuantity == null ? 0 : storedQuantity;
            validateStock(productId, currentQuantity + 1);
            if (currentQuantity > 0) {
                cartData.updateProductQuantity(cartId, productId, currentQuantity + 1);
            } else {
                cartData.addProductToCart(cartId, productId);
            }
            return;
        }
        throw new StoreException("Unable to add product ID " + productId + CART_ID_CONTEXT + cartId + ".");

    }

    @Override
    public void removeProductFromCart(Integer cartId, Integer productId) throws StoreException {
        if (Boolean.TRUE.equals(cartData.cartExists(cartId)) && Boolean.FALSE.equals(cartData.isCartSold(cartId))) {
            Integer storedQuantity = cartData.productQuantityInCart(cartId, productId);
            if (storedQuantity > 0) {
                cartData.removeProductFromCart(cartId, productId);
            } else {
                throw new StoreException("Product ID " + productId + " is not in cart ID " + cartId + ".");
            }
            return;
        }
        throw new StoreException("Unable to remove product ID " + productId + CART_ID_CONTEXT + cartId + ".");
    }

    @Override
    public List<CartItemDTO> listCartProducts(Integer id) throws StoreException {
        if (Boolean.TRUE.equals(cartData.cartExists(id))) {
            return cartData.listCartProducts(id);
        }
        throw new StoreException("Unable to list products in cart ID " + id + ".");
    }

    @Override
    public void changeProductQuantity(Integer cartId, Integer productId, Integer quantity) throws StoreException {
        if (Boolean.TRUE.equals(cartData.cartExists(cartId)) && Boolean.FALSE.equals(cartData.isCartSold(cartId))) {
            if (!Boolean.TRUE.equals(cartData.containsProduct(cartId, productId))) {
                throw new StoreException("The product is not in this cart.");
            }
            validateStock(productId, quantity);
            cartData.updateProductQuantity(cartId, productId, quantity);
            return;
        }
        throw new StoreException("Unable to update quantity for product ID " + productId + CART_ID_CONTEXT + cartId + ".");
    }

    @Override
    public Boolean isValidActiveCart(Integer id, Integer userId) throws StoreException {
        return Boolean.TRUE.equals(cartData.cartExists(id)) && Boolean.TRUE.equals(cartData.cartBelongsToUser(id, userId)) && Boolean.FALSE.equals(cartData.isCartSold(id)) && Boolean.TRUE.equals(cartData.cartItemCount(id) > 0);
    }

    @Override
    public Double getCartTotal(Integer id) throws StoreException {
        return cartData.getCartTotal(id);
    }

    @Override
    public PaginationDTO<List<CartItemDTO>> listCartProducts(Integer id, Integer pageSize, Integer pageNumber) throws StoreException {
        InputRules.page(pageSize, pageNumber);
        return cartData.listCartProducts(id, pageSize, pageNumber);
    }

    @Override
    public Integer productQuantityInCart(Integer id, Integer productId) throws StoreException {
        return cartData.productQuantityInCart(id, productId);
    }

    private void validateStock(Integer productId, Integer requestedQuantity) throws StoreException {
        if (requestedQuantity == null || requestedQuantity <= 0) {
            throw new StoreException("Cart quantity must be greater than zero.");
        }
        ProductDTO product = productData.findProductById(productId);
        if (product.getQuantity() == null || requestedQuantity > product.getQuantity()) {
            throw new StoreException("Requested quantity exceeds available stock.");
        }
    }
}

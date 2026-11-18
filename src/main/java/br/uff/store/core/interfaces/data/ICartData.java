package br.uff.store.core.interfaces.data;

import java.math.BigDecimal;

import java.util.List;

import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.dtos.CartItemDTO;
import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.exceptions.StoreException;

public interface ICartData {

    public CartDTO findCart(Integer id) throws StoreException;

    public List<CartDTO> listUnsoldUserCarts(Integer userId) throws StoreException;

    public PaginationDTO<List<CartItemDTO>> listCartProducts(Integer id, Integer pageSize, Integer pageNumber) throws StoreException;

    public Boolean isCartSold(Integer id) throws StoreException;

    public void assignUnownedCart(Integer id, Integer userId) throws StoreException;

    public Boolean isCartUnowned(Integer id) throws StoreException;

    public Boolean cartBelongsToUser(Integer id, Integer userId) throws StoreException;

    public Boolean cartExists(Integer id) throws StoreException;

    public Integer cartItemCount(Integer id) throws StoreException;

    public Integer productQuantityInCart(Integer id, Integer productId) throws StoreException;

    public void createCart(String ip, String createdAt, Integer userId) throws StoreException;

    public CartDTO findCart(String ip, String createdAt, Integer userId) throws StoreException;

    public List<CartItemDTO> listCartProducts(Integer id) throws StoreException;

    public Boolean containsProduct(Integer id, Integer productId) throws StoreException;

    public void addProductToCart(Integer id, Integer productId) throws StoreException;

    public void removeProductFromCart(Integer id, Integer productId) throws StoreException;

    public void updateProductQuantity(Integer id, Integer productId, Integer newQuantity) throws StoreException;

    public BigDecimal getCartTotal(Integer id) throws StoreException;
}

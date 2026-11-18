package br.uff.store.core.interfaces.services;

import java.math.BigDecimal;

import java.util.List;

import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.dtos.CartItemDTO;
import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.exceptions.StoreException;

public interface ICartService {

    public CartDTO getActiveCart(Integer cartId, Integer userId, String ip) throws StoreException;

    public void addProductToCart(Integer cartId, Integer productId) throws StoreException;

    public void removeProductFromCart(Integer cartId, Integer productId) throws StoreException;

    public List<CartItemDTO> listCartProducts(Integer id) throws StoreException;

    public PaginationDTO<List<CartItemDTO>> listCartProducts(Integer id, Integer pageSize, Integer pageNumber) throws StoreException;

    public void changeProductQuantity(Integer cartId, Integer productId, Integer quantity) throws StoreException;

    public Boolean isValidActiveCart(Integer id, Integer userId) throws StoreException;

    public BigDecimal getCartTotal(Integer id) throws StoreException;

    public Integer productQuantityInCart(Integer id, Integer productId) throws StoreException;
}

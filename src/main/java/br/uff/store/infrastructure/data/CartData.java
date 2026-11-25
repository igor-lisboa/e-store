package br.uff.store.infrastructure.data;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.dtos.CartItemDTO;
import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.ICartData;
import br.uff.store.infrastructure.database.MySQLDAO;
import java.util.Arrays;

public class CartData implements ICartData {

    private final MySQLDAO mysqlDAO;

    private static final String COUNT_ALL = "count(*)";
    private static final String CARTS_TABLE = "carts";
    private static final String CARTS_PRODUCTS_TABLE = "carts_products";
    private static final String CART_ERROR_PREFIX = "Unable to verify cart ID ";
    private static final String CART_ERROR_CONTEXT = " in cart ID ";
    private static final String CART_PRODUCTS_QUERY = "SELECT c.cart_id AS id, p.id AS productId, p.name AS name, p.description AS description, p.img AS image, p.price AS price, p.quantity AS availableStock, c.quantity AS quantity, p.price * c.quantity AS totalPrice FROM carts_products c LEFT JOIN products p ON (c.product_id = p.id) WHERE c.cart_id=?";

    public CartData() {
        this(new MySQLDAO());
    }

    public CartData(MySQLDAO mysqlDAO) {
        this.mysqlDAO = mysqlDAO;
    }

    @Override
    public CartDTO findCart(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            List<HashMap<String, Object>> result = this.mysqlDAO.query("SELECT id, user_id AS userId, created_at AS createdAt, ip FROM carts WHERE id=?", bind);
            return RowMapper.cart(result.get(0));
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public List<CartDTO> listUnsoldUserCarts(Integer userId) throws StoreException {
        try {
            Object[] bind = {userId};
            List<HashMap<String, Object>> rows = this.mysqlDAO.query("SELECT id, user_id AS userId, created_at AS createdAt, ip FROM carts WHERE user_id=? AND id NOT IN (SELECT cart_id FROM sales) ORDER BY id DESC", bind);
            List<CartDTO> mappedResults = new ArrayList<>();
            rows.forEach(cart -> mappedResults.add(RowMapper.cart(cart)));
            return mappedResults;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public Boolean isCartSold(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.scalar(COUNT_ALL, CARTS_TABLE, "id=? AND id IN (SELECT cart_id FROM sales)", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public void assignUnownedCart(Integer id, Integer userId) throws StoreException {
        try {
            Object[] bind = {userId, id};
            this.mysqlDAO.executeUpdate("UPDATE carts SET user_id=? WHERE id=? AND user_id IS NULL", bind);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public Boolean isCartUnowned(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.scalar(COUNT_ALL, CARTS_TABLE, "id=? AND user_id IS NULL", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public Boolean cartBelongsToUser(Integer id, Integer userId) throws StoreException {
        try {
            Object[] bind = {id, userId};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.scalar(COUNT_ALL, CARTS_TABLE, "id=? AND user_id=?", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public Boolean cartExists(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.scalar(COUNT_ALL, CARTS_TABLE, "id=?", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public Integer cartItemCount(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.scalar(COUNT_ALL, CARTS_PRODUCTS_TABLE, "cart_id=?", bind)));
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public Integer productQuantityInCart(Integer id, Integer productId) throws StoreException {
        try {
            Object[] bind = {productId, id};
            String quantity = String.valueOf(this.mysqlDAO.scalar("quantity", CARTS_PRODUCTS_TABLE, "product_id=? AND cart_id=?", bind));
            return Integer.valueOf(quantity.equals("null") ? "0" : quantity);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public void createCart(String ip, String createdAt, Integer userId) throws StoreException {
        try {
            Object[] bind = {ip, createdAt, createdAt, userId};
            this.mysqlDAO.executeUpdate("INSERT INTO carts (ip,created_at,last_activity_at,user_id) VALUES (?,?,?,?)", bind);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public CartDTO findCart(String ip, String createdAt, Integer userId) throws StoreException {
        try {
            ArrayList<Object> bind = new ArrayList<Object>(Arrays.asList(ip, createdAt));
            if (userId != null) {
                bind.add(userId);
            }
            List<HashMap<String, Object>> result = this.mysqlDAO.query("SELECT id, user_id AS userId, created_at AS createdAt, ip FROM carts WHERE ip=? AND created_at=? AND user_id" + (userId == null ? " IS NULL " : "=?") + " ORDER BY id DESC LIMIT 1", bind.toArray());
            return RowMapper.cart(result.get(0));
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public List<CartItemDTO> listCartProducts(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            List<HashMap<String, Object>> rows = this.mysqlDAO.query(CART_PRODUCTS_QUERY, bind);
            List<CartItemDTO> mappedResults = new ArrayList<>();
            rows.forEach(cart -> mappedResults.add(RowMapper.cartItem(cart)));
            return mappedResults;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public Boolean containsProduct(Integer id, Integer productId) throws StoreException {
        try {
            Object[] bind = {productId, id};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.scalar(COUNT_ALL, CARTS_PRODUCTS_TABLE, "product_id=? AND cart_id=?", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public void addProductToCart(Integer id, Integer productId) throws StoreException {
        try {
            Object[] bind = {productId, id};
            this.mysqlDAO.executeUpdate("INSERT INTO carts_products (product_id,cart_id,quantity) VALUES (?,?,1)", bind);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public void removeProductFromCart(Integer id, Integer productId) throws StoreException {
        try {
            Object[] bind = {productId, id};
            this.mysqlDAO.executeUpdate("DELETE FROM carts_products WHERE product_id=? AND cart_id=?", bind);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public void updateProductQuantity(Integer id, Integer productId, Integer newQuantity) throws StoreException {
        try {
            Object[] bind = {newQuantity, productId, id};
            this.mysqlDAO.executeUpdate("UPDATE carts_products SET quantity=? WHERE product_id=? AND cart_id=?", bind);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public BigDecimal getCartTotal(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            String totalPrice = String.valueOf(this.mysqlDAO.scalar("totalPrice", "SELECT sum(p.price * c.quantity) AS totalPrice FROM carts_products c LEFT JOIN products p ON (c.product_id = p.id) WHERE c.cart_id=? GROUP BY c.cart_id", "", bind));
            return new BigDecimal(totalPrice.equals("null") ? "0" : totalPrice);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public void touchCart(Integer id, String activityAt) throws StoreException {
        try {
            Object[] bind = {activityAt, id};
            this.mysqlDAO.executeUpdate("UPDATE carts SET last_activity_at=? WHERE id=?", bind);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public Boolean isCartActiveSince(Integer id, String since) throws StoreException {
        try {
            Object[] bind = {id, since};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.scalar(COUNT_ALL, CARTS_TABLE, "id=? AND last_activity_at>=?", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    /** Stock caps each merged quantity; rows that end at zero are removed, and the source cart is emptied. */
    @Override
    public void mergeCart(Integer sourceCartId, Integer targetCartId) throws StoreException {
        String[] commands = {
            "UPDATE carts_products target JOIN carts_products source ON source.product_id = target.product_id AND source.cart_id = ? "
                + "JOIN products p ON p.id = target.product_id SET target.quantity = LEAST(target.quantity + source.quantity, p.quantity) WHERE target.cart_id = ?",
            "INSERT INTO carts_products (cart_id, product_id, quantity) SELECT ?, source.product_id, LEAST(source.quantity, p.quantity) "
                + "FROM carts_products source JOIN products p ON p.id = source.product_id WHERE source.cart_id = ? "
                + "AND source.product_id NOT IN (SELECT product_id FROM carts_products WHERE cart_id = ?)",
            "DELETE FROM carts_products WHERE cart_id = ? AND quantity <= 0",
            "DELETE FROM carts_products WHERE cart_id = ?"
        };
        Object[][] bindings = {
            {sourceCartId, targetCartId},
            {targetCartId, sourceCartId, targetCartId},
            {targetCartId},
            {sourceCartId}
        };
        try {
            this.mysqlDAO.executeTransaction(commands, bindings);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

    @Override
    public PaginationDTO<List<CartItemDTO>> listCartProducts(Integer id, Integer pageSize, Integer pageNumber) throws StoreException {
        try {
            Object[] bind = {id};

            String offset = "";
            if (pageNumber > 1) {
                Integer calcOffset = (pageNumber - 1) * pageSize;
                offset = " OFFSET " + calcOffset + " ";
            }

            String limit = " LIMIT " + pageSize + " ";

            List<HashMap<String, Object>> rows = this.mysqlDAO.query(CART_PRODUCTS_QUERY + limit + offset, bind);
            List<CartItemDTO> mappedResults = new ArrayList<>();
            rows.forEach(cart -> mappedResults.add(RowMapper.cartItem(cart)));

            Integer lastPage = Integer.valueOf(String.valueOf(this.mysqlDAO.scalar("ceil(count(*)/" + pageSize + ")", CART_PRODUCTS_QUERY, "", bind)));

            return new PaginationDTO<>(pageNumber, mappedResults, lastPage);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the cart database operation.", e);
        }
    }

}

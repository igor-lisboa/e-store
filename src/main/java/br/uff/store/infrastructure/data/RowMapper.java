package br.uff.store.infrastructure.data;

import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.dtos.CartItemDTO;
import br.uff.store.core.dtos.CategoryDTO;
import br.uff.store.core.dtos.CustomerProductDTO;
import br.uff.store.core.dtos.FeaturedProductDTO;
import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.dtos.ProductListDTO;
import br.uff.store.core.dtos.ProductReviewListDTO;
import br.uff.store.core.dtos.RoleDTO;
import br.uff.store.core.dtos.SaleDTO;
import br.uff.store.core.dtos.SaleLineDTO;
import br.uff.store.core.dtos.UserDTO;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;

/**
 * Turns database rows into the DTOs that core uses. The row shape (column aliases and string values) is a
 * persistence detail, so it lives here and core never sees a Map. Required columns must be present; optional
 * ones may be null.
 */
public final class RowMapper {
    private RowMapper() { }

    public static AddressDTO address(Map<String, Object> row) {
        AddressDTO address = new AddressDTO();
        address.setId(integer(row, "id"));
        address.setUserId(integer(row, "userId"));
        address.setName(text(row, "name"));
        address.setPostalCode(integer(row, "postalCode"));
        address.setStreetAddress(text(row, "streetAddress"));
        address.setState(text(row, "state"));
        address.setCity(text(row, "city"));
        address.setCountry(text(row, "country"));
        return address;
    }

    public static CartDTO cart(Map<String, Object> row) {
        CartDTO cart = new CartDTO();
        cart.setId(integer(row, "id"));
        cart.setIp(text(row, "ip"));
        cart.setUserId(optionalInteger(row, "userId"));
        cart.setCreatedAt(date(row, "createdAt"));
        return cart;
    }

    public static CartItemDTO cartItem(Map<String, Object> row) {
        CartItemDTO item = new CartItemDTO();
        item.setId(integer(row, "id"));
        item.setProductId(integer(row, "productId"));
        item.setName(text(row, "name"));
        item.setDescription(text(row, "description"));
        item.setImage(text(row, "image"));
        item.setPrice(decimal(row, "price"));
        item.setAvailableStock(integer(row, "availableStock"));
        item.setQuantity(integer(row, "quantity"));
        item.setTotalPrice(decimal(row, "totalPrice"));
        return item;
    }

    public static CategoryDTO category(Map<String, Object> row) {
        CategoryDTO category = new CategoryDTO();
        category.setId(integer(row, "id"));
        category.setName(text(row, "name"));
        return category;
    }

    public static RoleDTO role(Map<String, Object> row) {
        RoleDTO role = new RoleDTO();
        role.setId(integer(row, "id"));
        role.setName(text(row, "name"));
        return role;
    }

    public static UserDTO user(Map<String, Object> row) {
        UserDTO user = new UserDTO();
        user.setId(integer(row, "id"));
        user.setName(text(row, "name"));
        user.setEmail(text(row, "email"));
        user.setPassword(text(row, "password"));
        user.setRoleId(integer(row, "roleId"));
        return user;
    }

    public static ProductDTO product(Map<String, Object> row) {
        ProductDTO product = new ProductDTO();
        product.setId(integer(row, "id"));
        product.setName(text(row, "name"));
        product.setPrice(decimal(row, "price"));
        product.setDescription(text(row, "description"));
        product.setImage(text(row, "image"));
        product.setCategory(text(row, "category"));
        product.setCategoryId(integer(row, "categoryId"));
        product.setQuantity(integer(row, "quantity"));
        return product;
    }

    public static ProductListDTO productListItem(Map<String, Object> row) {
        ProductListDTO product = new ProductListDTO();
        product.setId(integer(row, "id"));
        product.setName(text(row, "name"));
        product.setPrice(decimal(row, "price"));
        product.setImage(text(row, "image"));
        product.setCategory(text(row, "category"));
        product.setAvailable(truthy(row.get("available")));
        return product;
    }

    public static FeaturedProductDTO featuredProduct(Map<String, Object> row) {
        FeaturedProductDTO product = new FeaturedProductDTO();
        product.setId(integer(row, "id"));
        product.setName(text(row, "name"));
        product.setDescription(text(row, "description"));
        product.setImage(text(row, "image"));
        return product;
    }

    public static CustomerProductDTO customerProduct(Map<String, Object> row) {
        CustomerProductDTO product = new CustomerProductDTO();
        product.setId(integer(row, "id"));
        product.setName(text(row, "name"));
        product.setPrice(decimal(row, "price"));
        product.setDescription(text(row, "description"));
        product.setImage(text(row, "image"));
        product.setCategory(text(row, "category"));
        product.setQuantity(integer(row, "quantity"));
        product.setFavorite(integer(row, "favorite") > 0);
        product.setUserRating(optionalInteger(row, "userRating"));
        product.setRatingCount(integer(row, "ratingCount"));
        product.setRatingSum(integer(row, "ratingSum"));
        product.setRatingCount1(integer(row, "ratingCount1"));
        product.setRatingCount2(integer(row, "ratingCount2"));
        product.setRatingCount3(integer(row, "ratingCount3"));
        product.setRatingCount4(integer(row, "ratingCount4"));
        product.setRatingCount5(integer(row, "ratingCount5"));
        product.calculateRatingSummary();
        return product;
    }

    public static ProductReviewListDTO review(Map<String, Object> row) {
        ProductReviewListDTO review = new ProductReviewListDTO();
        review.setReviewer(text(row, "reviewer"));
        review.setTitle(text(row, "title"));
        review.setRating(text(row, "rating"));
        review.setDescription(text(row, "description"));
        review.setDisplayDate(text(row, "displayDate"));
        review.setShortDate(text(row, "shortDate"));
        return review;
    }

    public static SaleDTO sale(Map<String, Object> row) {
        SaleDTO sale = new SaleDTO();
        sale.setId(integer(row, "id"));
        sale.setCartId(integer(row, "cartId"));
        sale.setTotalPrice(decimal(row, "totalPrice"));
        sale.setCreatedAt(date(row, "createdAt"));
        sale.setAddressId(integer(row, "addressId"));
        sale.setUserId(integer(row, "userId"));
        sale.setStatus(text(row, "status"));
        sale.setCancelledAt(date(row, "cancelledAt"));
        sale.setPaymentProvider(optionalText(row, "paymentProvider"));
        sale.setPaymentStatus(optionalText(row, "paymentStatus"));
        sale.setDeliveryMethod(optionalText(row, "deliveryMethod"));
        sale.setTrackingCode(optionalText(row, "trackingCode"));
        if (row.get("deliveryStreet") != null) {
            sale.setAddress(new AddressDTO(null, sale.getUserId(), integer(row, "deliveryPostalCode"),
                    text(row, "deliveryStreet"), text(row, "deliveryCity"), text(row, "deliveryState"),
                    text(row, "deliveryCountry")));
        }
        return sale;
    }

    public static SaleLineDTO saleLine(Map<String, Object> row) {
        return new SaleLineDTO(integer(row, "id"), optionalInteger(row, "productId"), text(row, "productName"),
                optionalText(row, "image"), decimal(row, "unitPrice"), integer(row, "quantity"));
    }

    private static Integer integer(Map<String, Object> row, String column) {
        return Integer.valueOf(String.valueOf(row.get(column)));
    }

    private static Integer optionalInteger(Map<String, Object> row, String column) {
        Object value = row.get(column);
        return value == null || "null".equals(value.toString()) ? null : Integer.valueOf(value.toString());
    }

    private static String text(Map<String, Object> row, String column) {
        return String.valueOf(row.get(column));
    }

    private static String optionalText(Map<String, Object> row, String column) {
        Object value = row.get(column);
        return value == null ? null : String.valueOf(value);
    }

    private static BigDecimal decimal(Map<String, Object> row, String column) {
        return new BigDecimal(String.valueOf(row.get(column)));
    }

    /** Database timestamps arrive as "yyyy-MM-dd HH:mm:ss" and are read as UTC; unreadable values become null. */
    private static Date date(Map<String, Object> row, String column) {
        Object value = row.get(column);
        if (value == null) {
            return null;
        }
        try {
            return Date.from(LocalDateTime.parse(value.toString().replace(' ', 'T')).toInstant(ZoneOffset.UTC));
        } catch (DateTimeException invalid) {
            return null;
        }
    }

    private static boolean truthy(Object value) {
        String text = String.valueOf(value);
        return "1".equals(text) || "true".equalsIgnoreCase(text);
    }
}

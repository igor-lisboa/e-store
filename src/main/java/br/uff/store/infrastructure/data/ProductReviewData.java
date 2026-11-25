package br.uff.store.infrastructure.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.uff.store.core.dtos.ProductReviewInputDTO;
import br.uff.store.core.dtos.ProductReviewListDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IProductReviewData;
import br.uff.store.infrastructure.database.MySQLDAO;

public class ProductReviewData implements IProductReviewData {

    private final MySQLDAO mysqlDAO;

    public ProductReviewData() {
        this(new MySQLDAO());
    }

    public ProductReviewData(MySQLDAO mysqlDAO) {
        this.mysqlDAO = mysqlDAO;
    }

    @Override
    public ProductReviewInputDTO findUserReview(Integer userId, Integer productId) throws StoreException {
        try {
            Object[] bind = {userId, productId};
            List<HashMap<String, Object>> rows = this.mysqlDAO.query(
                    "SELECT rating, description, title FROM product_reviews WHERE user_id=? AND product_id=?", bind);
            if (rows.isEmpty()) {
                return null;
            }
            HashMap<String, Object> row = rows.get(0);
            return new ProductReviewInputDTO(userId, productId, Integer.valueOf(String.valueOf(row.get("rating"))),
                    (String) row.get("description"), (String) row.get("title"));
        } catch (Exception failure) {
            throw new StoreException("Unable to complete the product review database operation.", failure);
        }
    }

    @Override
    public int updateProductReview(ProductReviewInputDTO reviewInput) throws StoreException {
        try {
            Object[] bind = {reviewInput.getRating(), reviewInput.getDescription(), reviewInput.getTitle(),
                reviewInput.getUserId(), reviewInput.getProductId()};
            return this.mysqlDAO.executeUpdate(
                    "UPDATE product_reviews SET rating=?, description=?, title=? WHERE user_id=? AND product_id=?", bind);
        } catch (Exception failure) {
            throw new StoreException("Unable to complete the product review database operation.", failure);
        }
    }

    @Override
    public int deleteProductReview(Integer userId, Integer productId) throws StoreException {
        try {
            Object[] bind = {userId, productId};
            return this.mysqlDAO.executeUpdate("DELETE FROM product_reviews WHERE user_id=? AND product_id=?", bind);
        } catch (Exception failure) {
            throw new StoreException("Unable to complete the product review database operation.", failure);
        }
    }

    @Override
    public Boolean hasReviewedProduct(Integer userId, Integer productId) throws StoreException {
        try {
            Object[] bind = {userId, productId};
            Integer reviewCount = Integer.valueOf(String.valueOf(this.mysqlDAO.scalar("count(*)", "product_reviews", "user_id=? AND product_id=?", bind)));
            return reviewCount > 0;
        } catch (Exception failure) {
            throw new StoreException("Unable to complete the product review database operation.", failure);
        }
    }

    @Override
    public void insertProductReview(ProductReviewInputDTO reviewInput) throws StoreException {
        try {
            Object[] bind = {reviewInput.getUserId(), reviewInput.getProductId(), reviewInput.getRating(), reviewInput.getDescription(), reviewInput.getTitle()};
            this.mysqlDAO.executeUpdate("INSERT INTO product_reviews (user_id,product_id,rating,description,title,created_at) VALUES (?,?,?,?,?,SYSDATE())", bind);
        } catch (Exception failure) {
            throw new StoreException("Unable to complete the product review database operation.", failure);
        }
    }

    @Override
    public List<ProductReviewListDTO> listProductReviews(Integer productId) throws StoreException {
        try {
            Object[] bind = {productId};
            List<HashMap<String, Object>> rows = this.mysqlDAO.query("SELECT  u.`name` AS reviewer,  r.title AS title,  r.rating AS rating,  r.description AS description,  DATE_FORMAT(r.created_at, '%M %d, %Y') AS displayDate,  DATE_FORMAT(r.created_at, '%d/%m/%Y') AS shortDate  FROM  product_reviews r  LEFT JOIN users u ON (r.user_id = u.id)  WHERE  r.product_id = ?  ORDER BY  r.created_at", bind);
            List<ProductReviewListDTO> mappedResults = new ArrayList<>();
            rows.forEach(rating -> mappedResults.add(RowMapper.review(rating)));
            return mappedResults;
        } catch (Exception failure) {
            throw new StoreException("Unable to complete the product review database operation.", failure);
        }
    }
}

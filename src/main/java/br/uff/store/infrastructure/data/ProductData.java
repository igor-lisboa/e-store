package br.uff.store.infrastructure.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.uff.store.core.dtos.ProductFilterDTO;
import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.dtos.FeaturedProductDTO;
import br.uff.store.core.dtos.ProductListDTO;
import br.uff.store.core.dtos.CustomerProductDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.exceptions.ResourceNotFoundException;
import br.uff.store.core.interfaces.data.IProductData;
import br.uff.store.infrastructure.database.MySQLDAO;

public class ProductData implements IProductData {

    private final MySQLDAO mysqlDAO;
    private static final String PRODUCT_QUERY = "SELECT p.id AS id, p.name AS name, p.price AS price, p.description AS description,p.img AS image, p.category_id AS categoryId, p.quantity AS quantity, c.category_name AS category FROM products p LEFT JOIN vw_category c ON (p.category_id = c.id)";

    public ProductData() {
        this(new MySQLDAO());
    }

    public ProductData(MySQLDAO mysqlDAO) {
        this.mysqlDAO = java.util.Objects.requireNonNull(mysqlDAO, "Database access is required.");
    }

    @Override
    public ProductDTO findProductById(Integer id) throws StoreException {
        try {
            Object[] bind = { id };
            List<HashMap<String, Object>> result = this.mysqlDAO.query(PRODUCT_QUERY + " WHERE p.id=?", bind);
            if (result.isEmpty()) {
                throw new ResourceNotFoundException("Product");
            }
            return new ProductDTO(result.get(0));
        } catch (ResourceNotFoundException missing) {
            throw missing;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the product database operation.", e);
        }
    }

    @Override
    public void deleteProductById(Integer id) throws StoreException {
        try {
            Object[] bind = { id };
            this.mysqlDAO.executeUpdate("DELETE FROM products WHERE id=?", bind);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the product database operation.", e);
        }
    }

    @Override
    public void updateProductById(Integer id, ProductDTO product) throws StoreException {
        try {
            Object[] bind = { product.getName(), product.getPrice(), product.getDescription(), product.getImage(),
                    product.getCategoryId(), id };
            this.mysqlDAO.executeUpdate("UPDATE products SET name=?, price=?, description=?, img=?, category_id=? WHERE id=?",
                    bind);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the product database operation.", e);
        }
    }

    @Override
    public void addProductStock(Integer id, Integer quantity) throws StoreException {
        try {
            Object[] bind = { quantity, id };
            this.mysqlDAO.executeUpdate("UPDATE products SET quantity = quantity + ? WHERE id=?", bind);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the product database operation.", e);
        }
    }

    @Override
    public void insertProduct(ProductDTO product) throws StoreException {
        try {
            Object[] bind = { product.getName(), product.getPrice(), product.getDescription(), product.getImage(),
                    product.getCategoryId() };
            this.mysqlDAO.executeUpdate(
                    "INSERT INTO products (name,price,description,img,category_id,created_at,quantity) VALUES (?,?,?,?,?,SYSDATE(),0)",
                    bind);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the product database operation.", e);
        }
    }

    @Override
    public List<ProductDTO> listAdminProducts() throws StoreException {
        try {
            Object[] bind = {};
            List<HashMap<String, Object>> rows = this.mysqlDAO.query(PRODUCT_QUERY, bind);
            List<ProductDTO> mappedResults = new ArrayList<>();
            rows.forEach(product -> mappedResults.add(new ProductDTO(product)));
            return mappedResults;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the product database operation.", e);
        }
    }

    @Override
    public PaginationDTO<List<ProductListDTO>> listStorefrontProducts(ProductFilterDTO productFilter)
            throws StoreException {
        try {
            if (productFilter.getPageNumber() < 1) {
                productFilter.setPageNumber(1);
            }

            ArrayList<Object> bind = new ArrayList<>();
            String query = "SELECT p.id AS id,p.name AS name,p.price AS price,p.img AS image,c.category_name AS category FROM products p LEFT JOIN vw_category c on(p.category_id=c.id) ";

            String filter = "";
            String limit = " LIMIT " + productFilter.getPageSize() + " ";

            String whereTxt = " WHERE ";
            String andTXT = " AND ";
            String orTxt = " OR ";

            if (Boolean.TRUE.equals(productFilter.getFavoritesOnly())) {

                if (productFilter.getUserId() != null) {
                    filter += (filter.equals("") ? whereTxt : andTXT)
                            + " p.id in (SELECT f.product_id FROM favorite_products f WHERE f.user_id=?) ";
                    bind.add(productFilter.getUserId());
                } else {
                    throw new StoreException("Sign in to browse favorite products.");
                }
            }

            if (productFilter.getCategories() != null && !productFilter.getCategories().isEmpty()) {
                StringBuilder categoryFilter = new StringBuilder();
                for (String value : productFilter.getCategories()) {
                    bind.add("%" + value.toUpperCase() + "%");
                    bind.add(value);
                    categoryFilter.append((categoryFilter.toString().equals("") ? " ( " : orTxt)
                            + " UPPER(c.category_name) LIKE ? OR c.id = ? ");
                }
                categoryFilter.append((categoryFilter.toString().equals("") ? "" : " ) "));
                filter += (filter.equals("") ? whereTxt : andTXT) + categoryFilter.toString();
            }

            if (productFilter.getSubcategories() != null && !productFilter.getSubcategories().isEmpty()) {
                StringBuilder subcategoryFilter = new StringBuilder();
                for (String value : productFilter.getSubcategories()) {
                    bind.add("%" + value.toUpperCase() + "%");
                    bind.add(value);
                    subcategoryFilter.append((subcategoryFilter.toString().equals("") ? " ( " : orTxt)
                            + " UPPER(c.category_name) LIKE ? OR c.id = ? ");
                }
                subcategoryFilter.append((subcategoryFilter.toString().equals("") ? "" : " ) "));
                filter += (filter.equals("") ? whereTxt : andTXT) + subcategoryFilter.toString();
            }

            if (Boolean.TRUE.equals(productFilter.getNewReleasesOnly())) {
                filter += (filter.equals("") ? whereTxt : andTXT)
                        + " p.created_at >= DATE_SUB(NOW(), INTERVAL 1 MONTH) ";
            }

            if (productFilter.getSearch() != null) {
                bind.add("%" + productFilter.getSearch().toUpperCase() + "%");
                bind.add("%" + productFilter.getSearch().toUpperCase() + "%");
                filter += (filter.equals("") ? whereTxt : andTXT)
                        + " (UPPER(p.name) LIKE ? OR UPPER(p.description) LIKE ?) ";
            }

            filter += (filter.equals("") ? whereTxt : andTXT) + " p.quantity>0 ";

            if (productFilter.getMinimumPrice() != null) {
                bind.add(productFilter.getMinimumPrice());
                filter += (filter.equals("") ? whereTxt : andTXT) + " p.price >= ? ";
            }

            if (productFilter.getMaximumPrice() != null) {
                bind.add(productFilter.getMaximumPrice());
                filter += (filter.equals("") ? whereTxt : andTXT) + " p.price <= ? ";
            }

            String offset = "";
            if (productFilter.getPageNumber() > 1) {
                Integer calcOffset = (productFilter.getPageNumber() - 1) * productFilter.getPageSize();
                offset = " OFFSET " + calcOffset + " ";
            }

            List<HashMap<String, Object>> rows = this.mysqlDAO
                    .query(query + filter + limit + offset, bind.toArray());
            List<ProductListDTO> mappedResults = new ArrayList<>();
            rows.forEach(product -> mappedResults.add(new ProductListDTO(product)));

            Integer lastPage = Integer.valueOf(
                    String.valueOf(this.mysqlDAO.scalar("ceil(count(*)/" + productFilter.getPageSize() + ")",
                            query + filter, "", bind.toArray())));

            if (lastPage < 1) {
                lastPage = 1;
            }

            if (mappedResults.isEmpty()
                    && Boolean.FALSE.equals(productFilter.getPageNumber().equals(lastPage))) {
                productFilter.setPageNumber(lastPage);
                return this.listStorefrontProducts(productFilter);
            }

            return new PaginationDTO<>(productFilter.getPageNumber(), mappedResults, lastPage);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the product database operation.", e);
        }
    }

    @Override
    public CustomerProductDTO getCustomerProduct(Integer id, Integer userId)
            throws StoreException {
        try {
            Object[] bind = { String.valueOf(userId), String.valueOf(userId), id };
            List<HashMap<String, Object>> result = this.mysqlDAO.query(
                    "SELECT p.id, p.name AS name, p.quantity AS quantity, p.description AS description, p.price AS price, p.img AS image, c.category_name AS category, (SELECT count(*) FROM favorite_products f WHERE f.product_id = p.id AND f.user_id=?) favorite, (SELECT f.rating FROM product_reviews f WHERE f.product_id = p.id AND f.user_id=?) userRating, (SELECT count(*) FROM product_reviews f WHERE f.product_id = p.id) ratingCount, (SELECT COALESCE(sum(f.rating),0) FROM product_reviews f WHERE f.product_id = p.id) ratingSum, (SELECT count(*) FROM product_reviews f WHERE f.product_id = p.id AND f.rating='1') ratingCount1, (SELECT count(*) FROM product_reviews f WHERE f.product_id = p.id AND f.rating='2') ratingCount2, (SELECT count(*) FROM product_reviews f WHERE f.product_id = p.id AND f.rating='3') ratingCount3, (SELECT count(*) FROM product_reviews f WHERE f.product_id = p.id AND f.rating='4') ratingCount4, (SELECT count(*) FROM product_reviews f WHERE f.product_id = p.id AND f.rating='5') ratingCount5 FROM products p LEFT JOIN vw_category c ON (p.category_id = c.id) WHERE p.id=?",
                    bind);
            return new CustomerProductDTO(result.get(0));
        } catch (Exception e) {
            throw new StoreException("Unable to complete the product database operation.", e);
        }
    }

    @Override
    public Boolean isFavorite(Integer productId, Integer userId) throws StoreException {
        try {
            Object[] bind = { productId, userId };
            return Integer.valueOf(String.valueOf(
                    this.mysqlDAO.scalar("count(*)", "favorite_products", "product_id=? AND user_id=?", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the product database operation.", e);
        }
    }

    @Override
    public void removeFavorite(Integer productId, Integer userId) throws StoreException {
        try {
            Object[] bind = { productId, userId };
            this.mysqlDAO.executeUpdate("DELETE FROM favorite_products WHERE product_id=? AND user_id=?", bind);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the product database operation.", e);
        }
    }

    @Override
    public void addFavorite(Integer productId, Integer userId) throws StoreException {
        try {
            Object[] bind = { productId, userId };
            this.mysqlDAO.executeUpdate("INSERT INTO favorite_products (product_id,user_id) VALUES (?,?)", bind);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the product database operation.", e);
        }
    }

    @Override
    public List<FeaturedProductDTO> listFeaturedProducts() throws StoreException {
        try {
            Object[] bind = {};
            List<HashMap<String, Object>> rows = this.mysqlDAO.query(
                    "SELECT id, name AS name, description AS description, img AS image FROM products WHERE quantity > 0 ORDER BY quantity DESC LIMIT 3",
                    bind);
            List<FeaturedProductDTO> mappedResults = new ArrayList<>();
            rows.forEach(product -> mappedResults.add(new FeaturedProductDTO(product)));
            return mappedResults;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the product database operation.", e);
        }
    }
}

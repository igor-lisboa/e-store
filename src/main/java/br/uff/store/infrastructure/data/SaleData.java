package br.uff.store.infrastructure.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.dtos.SaleDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.ISaleData;
import br.uff.store.infrastructure.database.MySQLDAO;

public class SaleData implements ISaleData {

    private final MySQLDAO mysqlDAO;

    private static final String USER_SALES_QUERY = "SELECT id, cart_id AS cartId, total_price AS totalPrice, created_at AS createdAt, address_id AS addressId, user_id AS userId FROM sales WHERE user_id=? ORDER BY id DESC";

    public SaleData() {
        this.mysqlDAO = new MySQLDAO();
    }

    @Override
    public List<SaleDTO> listUserSales(Integer userId) throws StoreException {
        try {
            Object[] bind = {userId};
            List<HashMap<String, Object>> rows = this.mysqlDAO.query(USER_SALES_QUERY, bind);
            List<SaleDTO> mappedResults = new ArrayList<>();
            rows.forEach(sale -> mappedResults.add(new SaleDTO(sale)));
            return mappedResults;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the sale database operation.", e);
        }
    }

    @Override
    public void recordSale(Integer cartId, Integer addressId) throws StoreException {
        try {
            Object[] bind = {cartId, addressId};
            this.mysqlDAO.executeUpdate("CALL checkout_cart(?, ?)", bind);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the sale database operation.", e);
        }
    }

    @Override
    public PaginationDTO<List<SaleDTO>> listUserSales(Integer userId, Integer pageSize, Integer pageNumber) throws StoreException {
        try {
            Object[] bind = {userId};

            String offset = "";
            if (pageNumber > 1) {
                Integer calcOffset = (pageNumber - 1) * pageSize;
                offset = " OFFSET " + calcOffset + " ";
            }

            String limit = " LIMIT " + pageSize + " ";

            List<HashMap<String, Object>> rows = this.mysqlDAO.query(USER_SALES_QUERY + limit + offset, bind);
            List<SaleDTO> mappedResults = new ArrayList<>();
            rows.forEach(sale -> mappedResults.add(new SaleDTO(sale)));

            Integer lastPage = Integer.valueOf(String.valueOf(this.mysqlDAO.scalar("ceil(count(*)/" + pageSize + ")", USER_SALES_QUERY, "", bind)));

            return new PaginationDTO<>(pageNumber, mappedResults, lastPage);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the sale database operation.", e);
        }
    }
}

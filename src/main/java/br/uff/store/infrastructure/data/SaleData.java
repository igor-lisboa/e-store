package br.uff.store.infrastructure.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.uff.store.core.dtos.PaginationDTO;
import br.uff.store.core.dtos.DeliveryResultDTO;
import br.uff.store.core.dtos.PaymentResultDTO;
import br.uff.store.core.dtos.SaleDTO;
import br.uff.store.core.dtos.SaleLineDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.ISaleData;
import br.uff.store.infrastructure.database.MySQLDAO;

public class SaleData implements ISaleData {

    private final MySQLDAO mysqlDAO;

    private static final String USER_SALES_QUERY = "SELECT s.id, s.cart_id AS cartId, s.total_price AS totalPrice, s.created_at AS createdAt, s.address_id AS addressId, s.user_id AS userId, s.status, s.cancelled_at AS cancelledAt, p.provider AS paymentProvider, p.status AS paymentStatus, d.method AS deliveryMethod, d.tracking_code AS trackingCode, d.street AS deliveryStreet, d.city AS deliveryCity, d.state AS deliveryState, d.postal_code AS deliveryPostalCode, d.country AS deliveryCountry FROM sales s LEFT JOIN payments p ON p.sale_id = s.id LEFT JOIN deliveries d ON d.sale_id = s.id WHERE s.user_id=? ORDER BY s.id DESC";

    private static final String SALE_LINES_QUERY = "SELECT l.id, l.product_id AS productId, l.product_name AS productName, p.img AS image, l.unit_price AS unitPrice, l.quantity FROM sale_lines l LEFT JOIN products p ON p.id = l.product_id WHERE l.sale_id=? ORDER BY l.id";

    public SaleData() {
        this(new MySQLDAO());
    }

    public SaleData(MySQLDAO mysqlDAO) {
        this.mysqlDAO = mysqlDAO;
    }

    @Override
    public List<SaleLineDTO> listSaleLines(Integer saleId) throws StoreException {
        try {
            Object[] bind = {saleId};
            List<SaleLineDTO> lines = new ArrayList<>();
            this.mysqlDAO.query(SALE_LINES_QUERY, bind).forEach(line -> lines.add(RowMapper.saleLine(line)));
            return lines;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the sale database operation.", e);
        }
    }

    @Override
    public void cancelSale(Integer saleId, Integer userId) throws StoreException {
        try {
            Object[] bind = {saleId, userId};
            this.mysqlDAO.executeUpdate("CALL cancel_sale(?, ?)", bind);
        } catch (Exception e) {
            throw new StoreException("Unable to cancel the order. It may not exist or may no longer be cancellable.", e);
        }
    }

    @Override
    public List<SaleDTO> listUserSales(Integer userId) throws StoreException {
        try {
            Object[] bind = {userId};
            List<HashMap<String, Object>> rows = this.mysqlDAO.query(USER_SALES_QUERY, bind);
            List<SaleDTO> mappedResults = new ArrayList<>();
            rows.forEach(sale -> mappedResults.add(RowMapper.sale(sale)));
            return mappedResults;
        } catch (Exception e) {
            throw new StoreException("Unable to complete the sale database operation.", e);
        }
    }

    @Override
    public void recordSale(Integer cartId, Integer addressId, PaymentResultDTO payment, DeliveryResultDTO delivery)
            throws StoreException {
        try {
            Object[] bind = {cartId, addressId, payment.getProvider(), payment.getExternalReference(),
                payment.getStatus(), payment.getRawResponseRef(), delivery.getProvider(), delivery.getMethod(),
                delivery.getTrackingCode(), delivery.getStatus(), delivery.getRawResponseRef()};
            this.mysqlDAO.executeUpdate("CALL place_order(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", bind);
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
            rows.forEach(sale -> mappedResults.add(RowMapper.sale(sale)));

            Integer lastPage = Integer.valueOf(String.valueOf(this.mysqlDAO.scalar("ceil(count(*)/" + pageSize + ")", USER_SALES_QUERY, "", bind)));

            return new PaginationDTO<>(pageNumber, mappedResults, lastPage);
        } catch (Exception e) {
            throw new StoreException("Unable to complete the sale database operation.", e);
        }
    }
}

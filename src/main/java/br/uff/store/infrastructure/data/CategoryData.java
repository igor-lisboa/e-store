package br.uff.store.infrastructure.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.uff.store.core.dtos.CategoryDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.ICategoryData;
import br.uff.store.infrastructure.database.MySQLDAO;

public class CategoryData implements ICategoryData {

    private final MySQLDAO mysqlDAO;

    public CategoryData() {
        this.mysqlDAO = new MySQLDAO();
    }

    @Override
    public List<CategoryDTO> categories() throws StoreException {
        try {
            Object[] bind = {};
            List<HashMap<String, Object>> rows = this.mysqlDAO.query("SELECT id, category_name AS name FROM vw_category", bind);
            List<CategoryDTO> mappedResults = new ArrayList<>();
            rows.forEach(category -> mappedResults.add(RowMapper.category(category)));
            return mappedResults;
        } catch (Exception failure) {
            throw new StoreException("Unable to complete the category database operation.", failure);
        }
    }

}

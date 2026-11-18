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
    public List<CategoryDTO> listaCategorys() throws StoreException {
        try {
            Object[] bind = {};
            List<HashMap<String, Object>> retornoDesformatado = this.mysqlDAO.dbCarrega("SELECT id, category_name AS nome FROM vw_category", bind);
            List<CategoryDTO> retornoFormatado = new ArrayList<>();
            retornoDesformatado.forEach(categoria -> retornoFormatado.add(new CategoryDTO(categoria)));
            return retornoFormatado;
        } catch (Exception e) {
            throw new StoreException("Falha ao Listar as Categorys. (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

}

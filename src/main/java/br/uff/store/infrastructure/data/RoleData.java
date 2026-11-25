package br.uff.store.infrastructure.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.uff.store.core.dtos.RoleDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IRoleData;
import br.uff.store.infrastructure.database.MySQLDAO;

public class RoleData implements IRoleData {

    private final MySQLDAO mysqlDAO;

    public RoleData() {
        this.mysqlDAO = new MySQLDAO();
    }

    @Override
    public List<RoleDTO> listRoles() throws StoreException {
        try {
            Object[] bind = {};
            List<HashMap<String, Object>> rows = this.mysqlDAO.query("SELECT id, name AS name FROM roles", bind);
            List<RoleDTO> mappedResults = new ArrayList<>();
            rows.forEach(role -> mappedResults.add(RowMapper.role(role)));
            return mappedResults;
        } catch (Exception failure) {
            throw new StoreException("Unable to complete the role database operation.", failure);
        }
    }

}

package br.uff.store.infrastructure.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.enums.UserRole;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.exceptions.ResourceNotFoundException;
import br.uff.store.core.interfaces.data.IUserData;
import br.uff.store.infrastructure.database.MySQLDAO;

public class UserData implements IUserData {

    private final MySQLDAO mysqlDAO;
    private String tableUsers = "users";

    public UserData() {
        this(new MySQLDAO());
    }

    public UserData(MySQLDAO mysqlDAO) {
        this.mysqlDAO = java.util.Objects.requireNonNull(mysqlDAO, "Database access is required.");
    }

    @Override
    public UserDTO findUserById(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            List<HashMap<String, Object>> result = this.mysqlDAO.query("SELECT id, name AS name, email, password AS password, role_id AS roleId FROM users WHERE id=?", bind);
            if (result.isEmpty()) {
                throw new ResourceNotFoundException("User");
            }
            return RowMapper.user(result.get(0));
        } catch (ResourceNotFoundException missing) {
            throw missing;
        } catch (Exception failure) {
            throw new StoreException("Unable to complete the user database operation.", failure);
        }
    }

    @Override
    public List<UserDTO> listUsers() throws StoreException {
        try {
            Object[] bind = {};
            List<HashMap<String, Object>> rows = this.mysqlDAO.query("SELECT id, name AS name, email, password AS password, role_id AS roleId FROM users", bind);
            List<UserDTO> mappedResults = new ArrayList<>();
            rows.forEach(user -> mappedResults.add(RowMapper.user(user)));
            return mappedResults;
        } catch (Exception failure) {
            throw new StoreException("Unable to complete the user database operation.", failure);
        }
    }

    @Override
    public void updateUserById(Integer id, UserDTO user) throws StoreException {
        try {
            Object[] bind = {user.getName(), user.getEmail(), user.getPassword(), user.getRoleId(), id};
            this.mysqlDAO.executeUpdate("UPDATE users SET name=?,email=?,password=?,role_id=? WHERE id=?", bind);
        } catch (Exception failure) {
            throw new StoreException("Unable to complete the user database operation.", failure);
        }
    }

    @Override
    public UserDTO findUserByEmail(String email) throws StoreException {
        try {
            Object[] bind = {email};
            List<HashMap<String, Object>> rows = this.mysqlDAO.query(
                    "SELECT id, name AS name, email, password AS password, role_id AS roleId FROM users WHERE email=? LIMIT 1", bind);
            return rows.isEmpty() ? null : RowMapper.user(rows.get(0));
        } catch (Exception failure) {
            throw new StoreException("Failed to look up account credentials.");
        }
    }

    @Override
    public void updatePassword(Integer id, String password) throws StoreException {
        try {
            Object[] bind = {password, id};
            this.mysqlDAO.executeUpdate("UPDATE users SET password=? WHERE id=?", bind);
        } catch (Exception failure) {
            throw new StoreException("Failed to update account credentials.");
        }
    }

    @Override
    public void insertUser(UserDTO user) throws StoreException {
        try {
            Object[] bind = {user.getName(), user.getEmail(), user.getPassword(), user.getRoleId()};
            this.mysqlDAO.executeUpdate("INSERT INTO users (name,email,password,role_id) VALUES (?,?,?,?)", bind);
        } catch (Exception failure) {
            throw new StoreException("Unable to complete the user database operation.", failure);
        }
    }

    @Override
    public Boolean isAdmin(Integer id) throws StoreException {
        try {
            Object[] bind = {id, UserRole.ADMIN.getId()};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.scalar("count(*)", this.tableUsers, "id=? AND role_id=?", bind))) > 0;
        } catch (Exception failure) {
            throw new StoreException("Unable to complete the user database operation.", failure);
        }
    }

    @Override
    public Boolean isEmailInUse(String email, String ignoredUserId) throws StoreException {
        try {
            boolean creating = ignoredUserId == null || "null".equals(ignoredUserId);
            Object[] bind = creating ? new Object[] {email} : new Object[] {email, Integer.valueOf(ignoredUserId)};
            String filter = creating ? "email=?" : "email=? AND id<>?";
            return Integer.valueOf(String.valueOf(this.mysqlDAO.scalar("count(*)", this.tableUsers, filter, bind))) > 0;
        } catch (Exception failure) {
            throw new StoreException("Unable to complete the user database operation.", failure);
        }
    }

}

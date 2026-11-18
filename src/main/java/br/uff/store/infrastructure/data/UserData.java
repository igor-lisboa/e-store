package br.uff.store.infrastructure.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.enums.UserRole;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IUserData;
import br.uff.store.infrastructure.database.MySQLDAO;

public class UserData implements IUserData {

    private final MySQLDAO mysqlDAO;
    private String tableUsers = "users";

    public UserData() {
        this.mysqlDAO = new MySQLDAO();
    }

    @Override
    public UserDTO encontraUserPorId(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            List<HashMap<String, Object>> retorno = this.mysqlDAO.dbCarrega("SELECT id, name AS nome, email, password AS senha, role_id AS permissaoId FROM users WHERE id=?", bind);
            return new UserDTO(retorno.get(0));
        } catch (Exception e) {
            throw new StoreException("Falha ao Recuperar o Usuário de id: " + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public List<UserDTO> listaUsers() throws StoreException {
        try {
            Object[] bind = {};
            List<HashMap<String, Object>> retornoDesformatado = this.mysqlDAO.dbCarrega("SELECT id, name AS nome, email, password AS senha, role_id AS permissaoId FROM users", bind);
            List<UserDTO> retornoFormatado = new ArrayList<>();
            retornoDesformatado.forEach(usuario -> retornoFormatado.add(new UserDTO(usuario)));
            return retornoFormatado;
        } catch (Exception e) {
            throw new StoreException("Falha ao Listar Usuários. (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void atualizaUserPorId(Integer id, UserDTO usuario) throws StoreException {
        try {
            Object[] bind = {usuario.getNome(), usuario.getEmail(), usuario.getSenha(), usuario.getPermissaoId(), id};
            this.mysqlDAO.dbGrava("UPDATE users SET name=?,email=?,password=?,role_id=? WHERE id=?", bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao Atualizar o Usuário de id: " + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public UserDTO encontraUserPorEmail(String email) throws StoreException {
        try {
            Object[] bind = {email};
            List<HashMap<String, Object>> rows = this.mysqlDAO.dbCarrega(
                    "SELECT id, name AS nome, email, password AS senha, role_id AS permissaoId FROM users WHERE email=? LIMIT 1", bind);
            return rows.isEmpty() ? null : new UserDTO(rows.get(0));
        } catch (Exception e) {
            throw new StoreException("Failed to look up account credentials.");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void atualizaSenha(Integer id, String senha) throws StoreException {
        try {
            Object[] bind = {senha, id};
            this.mysqlDAO.dbGrava("UPDATE users SET password=? WHERE id=?", bind, false);
        } catch (Exception e) {
            throw new StoreException("Failed to update account credentials.");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void insereUser(UserDTO usuario) throws StoreException {
        try {
            Object[] bind = {usuario.getNome(), usuario.getEmail(), usuario.getSenha(), usuario.getPermissaoId()};
            this.mysqlDAO.dbGrava("INSERT INTO users (name,email,password,role_id) VALUES (?,?,?,?)", bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao Inserir o Usuário. (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public Boolean ehAdmin(Integer id) throws StoreException {
        try {
            Object[] bind = {id, UserRole.ADM.getId()};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.dbValor("count(*)", this.tableUsers, "id=? AND role_id=?", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException("Falha ao Verificar se o Usuário de id: " + id + " é Admin. (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public Boolean emailJaUsado(String email, String idPraIgnorar) throws StoreException {
        try {
            Object[] bind = {email, idPraIgnorar};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.dbValor("count(*)", this.tableUsers, "email=? AND id<>?", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException("Falha ao Verificar se o email: " + email + " está em uso por um id que não seja o id: " + idPraIgnorar + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

}

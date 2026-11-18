package br.uff.store.infrastructure.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.uff.store.core.dtos.AddressDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IAddressData;
import br.uff.store.infrastructure.database.MySQLDAO;

public class AddressData implements IAddressData {

    private final MySQLDAO mysqlDAO;

    public AddressData() {
        this.mysqlDAO = new MySQLDAO();
    }

    @Override
    public AddressDTO encontraAddressPorId(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            List<HashMap<String, Object>> retorno = this.mysqlDAO.dbCarrega("SELECT id, name AS nome, user_id AS usuarioId, zipcode AS cep, address AS logradouro, city AS cidade, state AS estado, country AS pais FROM address WHERE id=?", bind);
            return new AddressDTO(retorno.get(0));
        } catch (Exception e) {
            throw new StoreException("Falha ao Recuperar o Endereço de id: " + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void excluiAddressPorId(Integer id) throws StoreException {
        try {
            Object[] bind = {id};
            this.mysqlDAO.dbGrava("DELETE FROM address WHERE id=?", bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao Excluir o Endereço de id: " + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void atualizaAddressPorId(Integer id, AddressDTO endereco) throws StoreException {
        try {
            Object[] bind = {endereco.getNome(), endereco.getCep(), endereco.getLogradouro(), endereco.getCidade(), endereco.getEstado(), endereco.getPais(), id};
            this.mysqlDAO.dbGrava("UPDATE address SET name=?, zipcode=?, address=?, city=?, state=?, country=? WHERE id=?", bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao Atualizar o Endereço de id: " + id + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public void insereAddress(AddressDTO endereco) throws StoreException {
        try {
            Object[] bind = {endereco.getNome(), endereco.getUserId(), endereco.getCep(), endereco.getLogradouro(), endereco.getCidade(), endereco.getEstado(), endereco.getPais()};
            this.mysqlDAO.dbGrava("INSERT INTO address (name,user_id,zipcode,address,city,state,country) VALUES (?,?,?,?,?,?,?)", bind, false);
        } catch (Exception e) {
            throw new StoreException("Falha ao Inserir o Endereço de para o Usuário de id: " + endereco.getUserId() + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public List<AddressDTO> listaAddresssPorUserId(Integer usuarioId) throws StoreException {
        try {
            Object[] bind = {usuarioId};
            List<HashMap<String, Object>> retornoDesformatado = this.mysqlDAO.dbCarrega("SELECT id, name AS nome, user_id AS usuarioId, zipcode AS cep, address AS logradouro, city AS cidade, state AS estado, country AS pais FROM address WHERE user_id=?", bind);
            List<AddressDTO> retornoFormatado = new ArrayList<>();
            retornoDesformatado.forEach(endereco -> retornoFormatado.add(new AddressDTO(endereco)));
            return retornoFormatado;
        } catch (Exception e) {
            throw new StoreException("Falha ao Listar os Endereços para o usuário de id: " + usuarioId + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public Boolean verificaAddressDoUser(Integer id, Integer usuarioId) throws StoreException {
        try {
            Object[] bind = {id, usuarioId};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.dbValor("count(*)", "address", "id=? AND user_id=?", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException("Falha ao verificar se o endereço de id:" + id + " é do usuário de id: " + usuarioId + ". (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }

    @Override
    public Boolean enderecoFoiUsadoEmAlgumaSale(Integer enderecoId) throws StoreException {
        try {
            Object[] bind = {enderecoId};
            return Integer.valueOf(String.valueOf(this.mysqlDAO.dbValor("count(*)", "sales", "address_id=?", bind))) > 0;
        } catch (Exception e) {
            throw new StoreException("Falha ao Verificar se o Endereço de id: " + enderecoId + " foi usado em alguma Sale. (" + e.getMessage() + ")");
        } finally {
            this.mysqlDAO.destroyDb();
        }
    }
}

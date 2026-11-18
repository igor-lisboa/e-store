package br.uff.store.core.interfaces.data;

import java.util.List;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.exceptions.StoreException;

public interface IUserData {

    public UserDTO encontraUserPorId(Integer id) throws StoreException;

    public List<UserDTO> listaUsers() throws StoreException;

    public void atualizaUserPorId(Integer id, UserDTO usuario) throws StoreException;

    public UserDTO encontraUserPorEmail(String email) throws StoreException;

    public void atualizaSenha(Integer id, String senha) throws StoreException;

    public void insereUser(UserDTO usuario) throws StoreException;

    public Boolean emailJaUsado(String email, String idPraIgnorar) throws StoreException;

    public Boolean ehAdmin(Integer id) throws StoreException;
}

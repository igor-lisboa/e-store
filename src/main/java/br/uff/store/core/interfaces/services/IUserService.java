package br.uff.store.core.interfaces.services;

import java.util.List;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.exceptions.StoreException;

public interface IUserService {

    public UserDTO login(String email, String senha) throws StoreException;

    public UserDTO gravaUser(UserDTO usuario) throws StoreException;

    public List<UserDTO> listaUsers() throws StoreException;

    public UserDTO encontraUserPorId(Integer id) throws StoreException;

    public Boolean ehAdmin(Integer id) throws StoreException;
}

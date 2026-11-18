package br.uff.store.core.interfaces.services;

import java.util.List;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.exceptions.StoreException;

public interface IUserService {

    public UserDTO login(String email, String password) throws StoreException;

    public UserDTO saveUser(UserDTO user) throws StoreException;

    public List<UserDTO> listUsers() throws StoreException;

    public UserDTO findUserById(Integer id) throws StoreException;

    public Boolean isAdmin(Integer id) throws StoreException;
}

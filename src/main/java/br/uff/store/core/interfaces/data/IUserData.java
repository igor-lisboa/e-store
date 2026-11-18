package br.uff.store.core.interfaces.data;

import java.util.List;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.exceptions.StoreException;

public interface IUserData {

    public UserDTO findUserById(Integer id) throws StoreException;

    public List<UserDTO> listUsers() throws StoreException;

    public void updateUserById(Integer id, UserDTO user) throws StoreException;

    public UserDTO findUserByEmail(String email) throws StoreException;

    public void updatePassword(Integer id, String password) throws StoreException;

    public void insertUser(UserDTO user) throws StoreException;

    public Boolean isEmailInUse(String email, String ignoredUserId) throws StoreException;

    public Boolean isAdmin(Integer id) throws StoreException;
}

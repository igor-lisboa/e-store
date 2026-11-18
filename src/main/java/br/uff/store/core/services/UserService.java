package br.uff.store.core.services;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IUserData;
import br.uff.store.core.interfaces.services.IPasswordHasher;
import br.uff.store.core.interfaces.services.IUserService;
import br.uff.store.infrastructure.data.UserData;
import br.uff.store.infrastructure.security.Pbkdf2PasswordHasher;
import java.util.List;

public class UserService implements IUserService {
    private final IUserData usuarioData;
    private final IPasswordHasher passwordHasher;

    public UserService() {
        this(new UserData(), new Pbkdf2PasswordHasher());
    }

    public UserService(IUserData usuarioData, IPasswordHasher passwordHasher) {
        if (usuarioData == null || passwordHasher == null) {
            throw new IllegalArgumentException("User data and password hashing dependencies are required.");
        }
        this.usuarioData = usuarioData;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public UserDTO login(String email, String password) throws StoreException {
        UserDTO user = usuarioData.encontraUserPorEmail(email);
        if (user == null || !passwordHasher.matches(password, user.getSenha())) {
            throw new StoreException("Invalid email or password.");
        }
        if (!passwordHasher.isEncoded(user.getSenha())) {
            usuarioData.atualizaSenha(user.getId(), passwordHasher.hash(password));
        }
        user.setSenha(null);
        return user;
    }

    @Override
    public UserDTO gravaUser(UserDTO user) throws StoreException {
        if (Boolean.TRUE.equals(usuarioData.emailJaUsado(user.getEmail(), String.valueOf(user.getId())))) {
            throw new StoreException("The email address is already in use.");
        }

        if (user.getId() == null) {
            user.setSenha(passwordHasher.hash(user.getSenha()));
            usuarioData.insereUser(user);
            UserDTO savedUser = usuarioData.encontraUserPorEmail(user.getEmail());
            savedUser.setSenha(null);
            return savedUser;
        } else {
            if (user.getSenha() == null || user.getSenha().trim().isEmpty()) {
                String existingPassword = usuarioData.encontraUserPorId(user.getId()).getSenha();
                user.setSenha(passwordHasher.isEncoded(existingPassword)
                        ? existingPassword : passwordHasher.hash(existingPassword));
            } else {
                user.setSenha(passwordHasher.hash(user.getSenha()));
            }
            usuarioData.atualizaUserPorId(user.getId(), user);
        }

        UserDTO savedUser = usuarioData.encontraUserPorId(user.getId());
        savedUser.setSenha(null);
        return savedUser;
    }

    @Override
    public List<UserDTO> listaUsers() throws StoreException {
        List<UserDTO> users = usuarioData.listaUsers();
        users.forEach(user -> user.setSenha(null));
        return users;
    }

    @Override
    public UserDTO encontraUserPorId(Integer id) throws StoreException {
        UserDTO user = usuarioData.encontraUserPorId(id);
        user.setSenha(null);
        return user;
    }

    @Override
    public Boolean ehAdmin(Integer id) throws StoreException {
        return usuarioData.ehAdmin(id);
    }
}

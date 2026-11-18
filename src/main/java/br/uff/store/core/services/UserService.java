package br.uff.store.core.services;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IUserData;
import br.uff.store.core.interfaces.services.IPasswordHasher;
import br.uff.store.core.interfaces.services.IUserService;
import java.util.List;

public class UserService implements IUserService {
    private final IUserData userData;
    private final IPasswordHasher passwordHasher;

    public UserService(IUserData userData, IPasswordHasher passwordHasher) {
        if (userData == null || passwordHasher == null) {
            throw new IllegalArgumentException("User data and password hashing dependencies are required.");
        }
        this.userData = userData;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public UserDTO login(String email, String password) throws StoreException {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new StoreException("Invalid email or password.");
        }
        email = email.trim();
        UserDTO user = userData.findUserByEmail(email);
        if (user == null || !passwordHasher.matches(password, user.getPassword())) {
            throw new StoreException("Invalid email or password.");
        }
        if (!passwordHasher.isEncoded(user.getPassword())) {
            userData.updatePassword(user.getId(), passwordHasher.hash(password));
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public UserDTO saveUser(UserDTO user) throws StoreException {
        validateAccount(user);
        if (Boolean.TRUE.equals(userData.isEmailInUse(user.getEmail(), String.valueOf(user.getId())))) {
            throw new StoreException("The email address is already in use.");
        }

        if (user.getId() == null) {
            user.setPassword(passwordHasher.hash(user.getPassword()));
            userData.insertUser(user);
            UserDTO savedUser = userData.findUserByEmail(user.getEmail());
            savedUser.setPassword(null);
            return savedUser;
        } else {
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                String existingPassword = userData.findUserById(user.getId()).getPassword();
                user.setPassword(passwordHasher.isEncoded(existingPassword)
                        ? existingPassword : passwordHasher.hash(existingPassword));
            } else {
                user.setPassword(passwordHasher.hash(user.getPassword()));
            }
            userData.updateUserById(user.getId(), user);
        }

        UserDTO savedUser = userData.findUserById(user.getId());
        savedUser.setPassword(null);
        return savedUser;
    }

    @Override
    public List<UserDTO> listUsers() throws StoreException {
        List<UserDTO> users = userData.listUsers();
        users.forEach(user -> user.setPassword(null));
        return users;
    }

    @Override
    public UserDTO findUserById(Integer id) throws StoreException {
        UserDTO user = userData.findUserById(id);
        user.setPassword(null);
        return user;
    }

    @Override
    public Boolean isAdmin(Integer id) throws StoreException {
        return userData.isAdmin(id);
    }

    private void validateAccount(UserDTO user) throws StoreException {
        if (user == null) {
            throw new StoreException("Account details are required.");
        }
        InputRules.requiredText(user.getName(), 255, "Account name");
        InputRules.requiredText(user.getEmail(), 255, "Email address");
        if (!user.getEmail().trim().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new StoreException("A valid email address is required.");
        }
        if (!Integer.valueOf(1).equals(user.getRoleId()) && !Integer.valueOf(2).equals(user.getRoleId())) {
            throw new StoreException("A supported account role is required.");
        }
        if (user.getId() != null) {
            InputRules.positiveId(user.getId(), "User ID");
        } else if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new StoreException("A password is required for new accounts.");
        }
        user.setName(user.getName().trim());
        user.setEmail(user.getEmail().trim());
    }
}

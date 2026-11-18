package br.uff.loja.core.services;

import br.uff.loja.core.dtos.UsuarioDTO;
import br.uff.loja.core.exceptions.LojaException;
import br.uff.loja.core.interfaces.data.IUsuarioData;
import br.uff.loja.core.interfaces.services.IPasswordHasher;
import br.uff.loja.core.interfaces.services.IUsuarioService;
import br.uff.loja.infrastructure.data.UsuarioData;
import br.uff.loja.infrastructure.security.Pbkdf2PasswordHasher;
import java.util.List;

public class UsuarioService implements IUsuarioService {
    private final IUsuarioData usuarioData;
    private final IPasswordHasher passwordHasher;

    public UsuarioService() {
        this(new UsuarioData(), new Pbkdf2PasswordHasher());
    }

    public UsuarioService(IUsuarioData usuarioData, IPasswordHasher passwordHasher) {
        if (usuarioData == null || passwordHasher == null) {
            throw new IllegalArgumentException("User data and password hashing dependencies are required.");
        }
        this.usuarioData = usuarioData;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public UsuarioDTO login(String email, String password) throws LojaException {
        UsuarioDTO user = usuarioData.encontraUsuarioPorEmail(email);
        if (user == null || !passwordHasher.matches(password, user.getSenha())) {
            throw new LojaException("Invalid email or password.");
        }
        if (!passwordHasher.isEncoded(user.getSenha())) {
            usuarioData.atualizaSenha(user.getId(), passwordHasher.hash(password));
        }
        user.setSenha(null);
        return user;
    }

    @Override
    public UsuarioDTO gravaUsuario(UsuarioDTO user) throws LojaException {
        if (Boolean.TRUE.equals(usuarioData.emailJaUsado(user.getEmail(), String.valueOf(user.getId())))) {
            throw new LojaException("The email address is already in use.");
        }

        if (user.getId() == null) {
            user.setSenha(passwordHasher.hash(user.getSenha()));
            usuarioData.insereUsuario(user);
            UsuarioDTO savedUser = usuarioData.encontraUsuarioPorEmail(user.getEmail());
            savedUser.setSenha(null);
            return savedUser;
        } else {
            if (user.getSenha() == null || user.getSenha().trim().isEmpty()) {
                String existingPassword = usuarioData.encontraUsuarioPorId(user.getId()).getSenha();
                user.setSenha(passwordHasher.isEncoded(existingPassword)
                        ? existingPassword : passwordHasher.hash(existingPassword));
            } else {
                user.setSenha(passwordHasher.hash(user.getSenha()));
            }
            usuarioData.atualizaUsuarioPorId(user.getId(), user);
        }

        UsuarioDTO savedUser = usuarioData.encontraUsuarioPorId(user.getId());
        savedUser.setSenha(null);
        return savedUser;
    }

    @Override
    public List<UsuarioDTO> listaUsuarios() throws LojaException {
        List<UsuarioDTO> users = usuarioData.listaUsuarios();
        users.forEach(user -> user.setSenha(null));
        return users;
    }

    @Override
    public UsuarioDTO encontraUsuarioPorId(Integer id) throws LojaException {
        UsuarioDTO user = usuarioData.encontraUsuarioPorId(id);
        user.setSenha(null);
        return user;
    }

    @Override
    public Boolean ehAdmin(Integer id) throws LojaException {
        return usuarioData.ehAdmin(id);
    }
}

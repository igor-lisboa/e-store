package br.uff.loja.core.services;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.loja.core.dtos.UsuarioDTO;
import br.uff.loja.core.interfaces.data.IUsuarioData;
import br.uff.loja.core.interfaces.services.IPasswordHasher;
import org.junit.Test;

public class UsuarioServiceTest {
    private final IUsuarioData userData = mock(IUsuarioData.class);
    private final IPasswordHasher passwordHasher = mock(IPasswordHasher.class);
    private final UsuarioService service = new UsuarioService(userData, passwordHasher);

    @Test
    public void successfulLegacyLoginRehashesPasswordAndClearsItFromReturnedUser() throws Exception {
        UsuarioDTO user = new UsuarioDTO(5, "Customer", "customer@example.test", "legacy", 2);
        when(userData.encontraUsuarioPorEmail("customer@example.test")).thenReturn(user);
        when(passwordHasher.matches("legacy", "legacy")).thenReturn(true);
        when(passwordHasher.isEncoded("legacy")).thenReturn(false);
        when(passwordHasher.hash("legacy")).thenReturn("encoded-value");

        UsuarioDTO result = service.login("customer@example.test", "legacy");

        verify(userData).atualizaSenha(5, "encoded-value");
        assertSame(user, result);
        assertNull(result.getSenha());
    }

    @Test
    public void successfulAccountCreationStoresOnlyTheHashedPassword() throws Exception {
        UsuarioDTO input = new UsuarioDTO(null, "Customer", "customer@example.test", "plain", 2);
        UsuarioDTO saved = new UsuarioDTO(5, "Customer", "customer@example.test", "encoded-value", 2);
        when(userData.emailJaUsado("customer@example.test", "null")).thenReturn(false);
        when(passwordHasher.hash("plain")).thenReturn("encoded-value");
        when(userData.encontraUsuarioPorEmail("customer@example.test")).thenReturn(saved);

        UsuarioDTO result = service.gravaUsuario(input);

        verify(userData).insereUsuario(input);
        assertTrue(input.getSenha().equals("encoded-value"));
        assertNull(result.getSenha());
    }

    @Test
    public void blankPasswordOnAccountEditPreservesTheStoredHash() throws Exception {
        UsuarioDTO input = new UsuarioDTO(5, "Customer", "customer@example.test", "", 2);
        UsuarioDTO existing = new UsuarioDTO(5, "Customer", "customer@example.test", "stored-hash", 2);
        UsuarioDTO saved = new UsuarioDTO(5, "Customer", "customer@example.test", "stored-hash", 2);
        when(userData.emailJaUsado("customer@example.test", "5")).thenReturn(false);
        when(userData.encontraUsuarioPorId(5)).thenReturn(existing, saved);
        when(passwordHasher.isEncoded("stored-hash")).thenReturn(true);

        UsuarioDTO result = service.gravaUsuario(input);

        verify(userData).atualizaUsuarioPorId(eq(5), eq(input));
        assertTrue("stored-hash".equals(input.getSenha()));
        assertNull(result.getSenha());
    }
}

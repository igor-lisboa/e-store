package br.uff.store.core.services;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.interfaces.data.IUserData;
import br.uff.store.core.interfaces.services.IPasswordHasher;
import org.junit.Test;

public class UserServiceTest {
    private final IUserData userData = mock(IUserData.class);
    private final IPasswordHasher passwordHasher = mock(IPasswordHasher.class);
    private final UserService service = new UserService(userData, passwordHasher);

    @Test
    public void successfulLegacyLoginRehashesPasswordAndClearsItFromReturnedUser() throws Exception {
        UserDTO user = new UserDTO(5, "Customer", "customer@example.test", "legacy", 2);
        when(userData.encontraUserPorEmail("customer@example.test")).thenReturn(user);
        when(passwordHasher.matches("legacy", "legacy")).thenReturn(true);
        when(passwordHasher.isEncoded("legacy")).thenReturn(false);
        when(passwordHasher.hash("legacy")).thenReturn("encoded-value");

        UserDTO result = service.login("customer@example.test", "legacy");

        verify(userData).atualizaSenha(5, "encoded-value");
        assertSame(user, result);
        assertNull(result.getSenha());
    }

    @Test
    public void successfulAccountCreationStoresOnlyTheHashedPassword() throws Exception {
        UserDTO input = new UserDTO(null, "Customer", "customer@example.test", "plain", 2);
        UserDTO saved = new UserDTO(5, "Customer", "customer@example.test", "encoded-value", 2);
        when(userData.emailJaUsado("customer@example.test", "null")).thenReturn(false);
        when(passwordHasher.hash("plain")).thenReturn("encoded-value");
        when(userData.encontraUserPorEmail("customer@example.test")).thenReturn(saved);

        UserDTO result = service.gravaUser(input);

        verify(userData).insereUser(input);
        assertTrue(input.getSenha().equals("encoded-value"));
        assertNull(result.getSenha());
    }

    @Test
    public void blankPasswordOnAccountEditPreservesTheStoredHash() throws Exception {
        UserDTO input = new UserDTO(5, "Customer", "customer@example.test", "", 2);
        UserDTO existing = new UserDTO(5, "Customer", "customer@example.test", "stored-hash", 2);
        UserDTO saved = new UserDTO(5, "Customer", "customer@example.test", "stored-hash", 2);
        when(userData.emailJaUsado("customer@example.test", "5")).thenReturn(false);
        when(userData.encontraUserPorId(5)).thenReturn(existing, saved);
        when(passwordHasher.isEncoded("stored-hash")).thenReturn(true);

        UserDTO result = service.gravaUser(input);

        verify(userData).atualizaUserPorId(eq(5), eq(input));
        assertTrue("stored-hash".equals(input.getSenha()));
        assertNull(result.getSenha());
    }
}

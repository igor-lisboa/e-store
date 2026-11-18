package br.uff.store.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.exceptions.StoreException;
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
        when(userData.findUserByEmail("customer@example.test")).thenReturn(user);
        when(passwordHasher.matches("legacy", "legacy")).thenReturn(true);
        when(passwordHasher.isEncoded("legacy")).thenReturn(false);
        when(passwordHasher.hash("legacy")).thenReturn("encoded-value");

        UserDTO result = service.login("customer@example.test", "legacy");

        verify(userData).updatePassword(5, "encoded-value");
        assertSame(user, result);
        assertNull(result.getPassword());
    }

    @Test
    public void successfulAccountCreationStoresOnlyTheHashedPassword() throws Exception {
        UserDTO input = new UserDTO(null, "Customer", "customer@example.test", "plain-pass", 2);
        UserDTO saved = new UserDTO(5, "Customer", "customer@example.test", "encoded-value", 2);
        when(userData.isEmailInUse("customer@example.test", "null")).thenReturn(false);
        when(passwordHasher.hash("plain-pass")).thenReturn("encoded-value");
        when(userData.findUserByEmail("customer@example.test")).thenReturn(saved);

        UserDTO result = service.saveUser(input);

        verify(userData).insertUser(input);
        assertTrue(input.getPassword().equals("encoded-value"));
        assertNull(result.getPassword());
    }

    @Test
    public void blankPasswordOnAccountEditPreservesTheStoredHash() throws Exception {
        UserDTO input = new UserDTO(5, "Customer", "customer@example.test", "", 2);
        UserDTO existing = new UserDTO(5, "Customer", "customer@example.test", "stored-hash", 2);
        UserDTO saved = new UserDTO(5, "Customer", "customer@example.test", "stored-hash", 2);
        when(userData.isEmailInUse("customer@example.test", "5")).thenReturn(false);
        when(userData.findUserById(5)).thenReturn(existing, saved);
        when(passwordHasher.isEncoded("stored-hash")).thenReturn(true);

        UserDTO result = service.saveUser(input);

        verify(userData).updateUserById(eq(5), eq(input));
        assertTrue("stored-hash".equals(input.getPassword()));
        assertNull(result.getPassword());
    }

    @Test
    public void passwordsShorterThanEightCharactersAreRejectedBeforeHashingOrStorage() throws Exception {
        try {
            service.saveUser(new UserDTO(null, "Customer", "customer@example.test", "1234567", 2));
            org.junit.Assert.fail("A seven-character password was accepted.");
        } catch (StoreException expected) {
            assertEquals("Password must be at least 8 characters.", expected.getMessage());
        }
        verifyNoInteractions(passwordHasher, userData);
    }

    @Test
    public void eightCharacterPasswordIsTheAcceptedBoundary() throws Exception {
        when(userData.isEmailInUse("customer@example.test", "null")).thenReturn(false);
        when(passwordHasher.hash("12345678")).thenReturn("encoded-value");
        when(userData.findUserByEmail("customer@example.test"))
                .thenReturn(new UserDTO(5, "Customer", "customer@example.test", "encoded-value", 2));

        service.saveUser(new UserDTO(null, "Customer", "customer@example.test", "12345678", 2));

        verify(passwordHasher).hash("12345678");
    }

    @Test
    public void accountEditWithShortNewPasswordIsRejectedWithoutUpdatingTheRecord() throws Exception {
        when(userData.isEmailInUse("customer@example.test", "5")).thenReturn(false);
        try {
            service.saveUser(new UserDTO(5, "Customer", "customer@example.test", "short", 2));
            org.junit.Assert.fail("A short replacement password was accepted.");
        } catch (StoreException expected) {
            assertEquals("Password must be at least 8 characters.", expected.getMessage());
        }
        verify(userData, never()).updateUserById(eq(5), org.mockito.ArgumentMatchers.any(UserDTO.class));
        verifyNoInteractions(passwordHasher);
    }
}
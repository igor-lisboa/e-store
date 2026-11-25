package br.uff.store.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IUserData;
import br.uff.store.core.interfaces.services.IPasswordHasher;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/** Sign-in outcomes, account validation, and what leaves the service (never a password hash). */
public class UserServiceRulesTest {
    private final IUserData userData = mock(IUserData.class);
    private final IPasswordHasher hasher = mock(IPasswordHasher.class);
    private final UserService service = new UserService(userData, hasher);

    private void expectFailure(String message, UserDTO account) throws Exception {
        try {
            service.saveUser(account);
            fail("Expected: " + message);
        } catch (StoreException expected) {
            assertEquals(message, expected.getMessage());
        }
        verify(userData, never()).insertUser(any());
        verify(userData, never()).updateUserById(any(), any());
    }

    @Test
    public void signInWithAnUnknownEmailGivesTheSameAnswerAsAWrongPassword() throws Exception {
        when(userData.findUserByEmail("nobody@example.test")).thenReturn(null);
        UserDTO known = new UserDTO(5, "Ana", "ana@example.test", "stored", 2);
        when(userData.findUserByEmail("ana@example.test")).thenReturn(known);
        when(hasher.isEncoded("stored")).thenReturn(true);
        when(hasher.matches("wrong", "stored")).thenReturn(false);

        for (String[] attempt : new String[][] {{"nobody@example.test", "x"}, {"ana@example.test", "wrong"}}) {
            try {
                service.login(attempt[0], attempt[1]);
                fail("Sign-in must fail for " + attempt[0]);
            } catch (StoreException failure) {
                assertEquals("Invalid email or password.", failure.getMessage());
            }
        }
    }

    @Test
    public void aSuccessfulSignInTrimsTheEmailAndNeverReturnsTheHash() throws Exception {
        UserDTO user = new UserDTO(5, "Ana", "ana@example.test", "stored", 2);
        when(userData.findUserByEmail("ana@example.test")).thenReturn(user);
        when(hasher.isEncoded("stored")).thenReturn(true);
        when(hasher.matches("right-password", "stored")).thenReturn(true);

        UserDTO signedIn = service.login("  ana@example.test ", "right-password");

        assertSame(user, signedIn);
        assertNull(signedIn.getPassword());
        verify(userData, never()).updatePassword(any(), any());
    }

    @Test
    public void accountDetailsAreRequired() throws Exception {
        expectFailure("Account details are required.", null);
    }

    @Test
    public void aTakenEmailAddressIsRejectedBeforeAnythingIsStored() throws Exception {
        when(userData.isEmailInUse("ana@example.test", "null")).thenReturn(true);

        expectFailure("The email address is already in use.", new UserDTO(null, "Ana", "ana@example.test", "long-enough-password", 2));

        verifyNoInteractions(hasher);
    }

    @Test
    public void aMissingNameOrEmailIsRejected() throws Exception {
        expectFailure("Account name is required and must not exceed 255 characters.",
                new UserDTO(null, "  ", "ana@example.test", "long-enough-password", 2));
        expectFailure("Email address is required and must not exceed 255 characters.",
                new UserDTO(null, "Ana", null, "long-enough-password", 2));
    }

    @Test
    public void anExistingAccountMustCarryAPositiveId() throws Exception {
        expectFailure("User ID must be a positive integer.", new UserDTO(-3, "Ana", "ana@example.test", null, 2));
    }

    @Test
    public void namesAndEmailsAreTrimmedBeforeTheyAreSaved() throws Exception {
        UserDTO input = new UserDTO(null, "  Ana  ", "  ana@example.test ", "long-enough-password", 2);
        when(userData.isEmailInUse("ana@example.test", "null")).thenReturn(false);
        when(hasher.hash("long-enough-password")).thenReturn("hashed");
        when(userData.findUserByEmail("ana@example.test")).thenReturn(new UserDTO(9, "Ana", "ana@example.test", "hashed", 2));

        UserDTO saved = service.saveUser(input);

        assertEquals("Ana", input.getName());
        assertEquals("ana@example.test", input.getEmail());
        assertNull("the hash must not leave the service", saved.getPassword());
    }

    @Test
    public void editingAnAccountThatStillHasALegacyValueStoresItHashedNotAsPlainText() throws Exception {
        UserDTO edit = new UserDTO(5, "Ana", "ana@example.test", "", 2);
        when(userData.isEmailInUse("ana@example.test", "5")).thenReturn(false);
        when(userData.findUserById(5)).thenReturn(new UserDTO(5, "Ana", "ana@example.test", "legacy-plain", 2));
        when(hasher.isEncoded("legacy-plain")).thenReturn(false);
        when(hasher.hash("legacy-plain")).thenReturn("hashed-legacy");

        service.saveUser(edit);

        assertEquals("hashed-legacy", edit.getPassword());
        verify(userData).updateUserById(5, edit);
    }

    @Test
    public void listedAndFoundAccountsNeverExposeAPasswordHash() throws Exception {
        List<UserDTO> users = Arrays.asList(new UserDTO(1, "A", "a@example.test", "h1", 1), new UserDTO(2, "B", "b@example.test", "h2", 2));
        when(userData.listUsers()).thenReturn(users);
        when(userData.findUserById(2)).thenReturn(users.get(1));

        for (UserDTO user : service.listUsers()) {
            assertNull(user.getPassword());
        }
        assertNull(service.findUserById(2).getPassword());
    }

    @Test
    public void administratorStatusComesFromTheDataLayer() throws Exception {
        when(userData.isAdmin(1)).thenReturn(true);
        when(userData.isAdmin(2)).thenReturn(false);

        assertTrue(service.isAdmin(1));
        assertEquals(Boolean.FALSE, service.isAdmin(2));
    }

    @Test
    public void bothDependenciesAreRequired() {
        try {
            new UserService(userData, null);
            fail("A password hasher is required.");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }
}

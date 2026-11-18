package br.uff.store.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IUserData;
import br.uff.store.core.interfaces.services.IPasswordHasher;
import org.junit.Test;

public class AccountInputTest {
    private final IUserData users = mock(IUserData.class);
    private final IPasswordHasher hasher = mock(IPasswordHasher.class);
    private final UserService service = new UserService(users, hasher);

    @Test
    public void malformedEmailDoesNotReachCredentialStorage() throws Exception {
        assertRejected(new UserDTO(null, "Customer", "not-an-email", "password", 2),
                "A valid email address is required.");
    }

    @Test
    public void unsupportedRolesAreRejectedBeforeAnyWritesOrHashing() throws Exception {
        assertRejected(new UserDTO(null, "Customer", "customer@example.test", "password", 99),
                "A supported account role is required.");
    }

    @Test
    public void creatingAnAccountRequiresAPassword() throws Exception {
        assertRejected(new UserDTO(null, "Customer", "customer@example.test", "", 2),
                "A password is required for new accounts.");
    }

    @Test
    public void emptyLoginCredentialsAreRejectedWithoutDatabaseAccess() throws Exception {
        try {
            service.login("customer@example.test", "");
            fail("Empty login credentials were accepted.");
        } catch (StoreException failure) {
            assertEquals("Invalid email or password.", failure.getMessage());
            verifyZeroInteractions(users, hasher);
        }
    }

    private void assertRejected(UserDTO input, String expectedMessage) throws Exception {
        try {
            service.saveUser(input);
            fail("Invalid account details were accepted.");
        } catch (StoreException failure) {
            assertEquals(expectedMessage, failure.getMessage());
            verifyZeroInteractions(users, hasher);
        }
    }
}

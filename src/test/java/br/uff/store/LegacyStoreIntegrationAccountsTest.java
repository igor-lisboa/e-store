package br.uff.store;

import br.uff.store.application.StoreServices;
import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.exceptions.StoreException;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

public class LegacyStoreIntegrationAccountsTest extends StoreIntegrationFixture {
    @Test
    public void referenceRolesMatchPersistedAccountAuthorization() throws Exception {
        Map<Integer, String> roles = StoreServices.roles().listRoles().stream()
                .collect(Collectors.toMap(role -> role.getId(), role -> role.getName()));
        assertEquals("ADMIN", roles.get(1));
        assertEquals("CUSTOMER", roles.get(2));
        assertTrue(users.isAdmin(1));
        assertFalse(users.isAdmin(2));
    }

    @Test
    public void incorrectCredentialsCannotAuthenticateAnExistingAccount() throws Exception {
        StoreException failure = assertThrows(StoreException.class,
                () -> users.login("customer@example.test", "wrong-password"));
        assertEquals("Invalid email or password.", failure.getMessage());
    }

    @Test
    public void registeredAccountCanAuthenticateWithoutReturningCredentials() throws Exception {
        UserDTO saved = users.saveUser(new UserDTO(null, "New customer", "new@example.test", "new-password", 2));
        UserDTO authenticated = users.login("new@example.test", "new-password");
        assertEquals(saved.getId(), authenticated.getId());
        assertEquals("New customer", authenticated.getName());
        assertEquals(Integer.valueOf(2), authenticated.getRoleId());
        assertNull(saved.getPassword());
        assertNull(authenticated.getPassword());
    }

    @Test
    public void editingAnAccountWithBlankPasswordPreservesAuthentication() throws Exception {
        UserDTO customer = users.findUserById(2);
        customer.setName("Updated customer");
        customer.setPassword("");
        users.saveUser(customer);
        assertEquals("Updated customer", users.login("customer@example.test", "development-password").getName());
    }

    @Test
    public void duplicateEmailCreationLeavesOriginalAccountUnchanged() throws Exception {
        StoreException failure = assertThrows(StoreException.class, () -> users.saveUser(
                new UserDTO(null, "Duplicate", "customer@example.test", "duplicate-password", 2)));
        assertEquals("The email address is already in use.", failure.getMessage());
        assertEquals(2, users.listUsers().size());
        assertEquals(Integer.valueOf(2), users.login("customer@example.test", "development-password").getId());
    }

    @Test
    public void duplicateEmailEditDoesNotOverwriteAnotherAccount() throws Exception {
        UserDTO customer = users.findUserById(2);
        customer.setEmail("admin@example.test");
        assertThrows(StoreException.class, () -> users.saveUser(customer));
        assertEquals("customer@example.test", users.findUserById(2).getEmail());
        assertEquals("admin@example.test", users.findUserById(1).getEmail());
    }
}

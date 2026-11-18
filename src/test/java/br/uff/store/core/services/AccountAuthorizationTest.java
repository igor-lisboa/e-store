package br.uff.store.core.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import br.uff.store.core.enums.UserRole;
import org.junit.Test;

public class AccountAuthorizationTest {
    @Test
    public void usersCanEditTheirOwnAccount() {
        assertTrue(AccountAuthorization.canEdit(8, UserRole.CLIENTE.getId(), 8));
    }

    @Test
    public void administratorsCanEditAnotherAccount() {
        assertTrue(AccountAuthorization.canEdit(8, UserRole.ADM.getId(), 9));
    }

    @Test
    public void guestsAndUsersEditingAnotherAccountAreRejected() {
        assertFalse(AccountAuthorization.canEdit(null, null, 9));
        assertFalse(AccountAuthorization.canEdit(8, UserRole.CLIENTE.getId(), 9));
        assertFalse(AccountAuthorization.canEdit(8, UserRole.ADM.getId(), null));
    }
}

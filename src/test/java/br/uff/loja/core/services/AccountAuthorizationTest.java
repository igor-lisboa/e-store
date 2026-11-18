package br.uff.loja.core.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import br.uff.loja.core.enums.EPermissaoUsuario;
import org.junit.Test;

public class AccountAuthorizationTest {
    @Test
    public void usersCanEditTheirOwnAccount() {
        assertTrue(AccountAuthorization.canEdit(8, EPermissaoUsuario.CLIENTE.getId(), 8));
    }

    @Test
    public void administratorsCanEditAnotherAccount() {
        assertTrue(AccountAuthorization.canEdit(8, EPermissaoUsuario.ADM.getId(), 9));
    }

    @Test
    public void guestsAndUsersEditingAnotherAccountAreRejected() {
        assertFalse(AccountAuthorization.canEdit(null, null, 9));
        assertFalse(AccountAuthorization.canEdit(8, EPermissaoUsuario.CLIENTE.getId(), 9));
        assertFalse(AccountAuthorization.canEdit(8, EPermissaoUsuario.ADM.getId(), null));
    }
}

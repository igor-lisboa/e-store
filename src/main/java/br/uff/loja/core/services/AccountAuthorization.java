package br.uff.loja.core.services;

import br.uff.loja.core.enums.EPermissaoUsuario;

public final class AccountAuthorization {
    private AccountAuthorization() { }

    public static boolean canEdit(Integer activeUserId, Integer activeRoleId, Integer targetUserId) {
        if (activeUserId == null || activeRoleId == null || targetUserId == null) {
            return false;
        }
        return activeUserId.equals(targetUserId)
                || EPermissaoUsuario.ADM.getId().equals(activeRoleId);
    }
}

package br.uff.store.core.services;

import br.uff.store.core.enums.UserRole;

public final class AccountAuthorization {
    private AccountAuthorization() { }

    public static boolean canEdit(Integer activeUserId, Integer activeRoleId, Integer targetUserId) {
        if (activeUserId == null || activeRoleId == null || targetUserId == null) {
            return false;
        }
        return activeUserId.equals(targetUserId)
                || UserRole.ADM.getId().equals(activeRoleId);
    }
}

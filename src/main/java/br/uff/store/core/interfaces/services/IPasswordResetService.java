package br.uff.store.core.interfaces.services;

import br.uff.store.core.exceptions.StoreException;

public interface IPasswordResetService {
    /** Emails a reset link when the account exists; says nothing about whether it does. */
    void requestReset(String email, String resetPageUrl) throws StoreException;

    /** Sets a new password with a link token; a token works once and expires. */
    void resetPassword(String token, String newPassword) throws StoreException;
}

package br.uff.store.core.interfaces.data;

import br.uff.store.core.exceptions.StoreException;

public interface IPasswordResetData {
    /** Stores the hash of a new reset token for the user and discards the user's earlier unused tokens. */
    void storeToken(Integer userId, String tokenHash, int validMinutes) throws StoreException;

    /**
     * Marks an unused, unexpired token as used and returns its user ID, or null when the token is unknown,
     * already used, or expired. Two requests with the same token can never both succeed.
     */
    Integer redeemToken(String tokenHash) throws StoreException;
}

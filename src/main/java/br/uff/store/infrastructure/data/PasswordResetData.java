package br.uff.store.infrastructure.data;

import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IPasswordResetData;
import br.uff.store.infrastructure.database.MySQLDAO;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PasswordResetData implements IPasswordResetData {
    private final MySQLDAO mysqlDAO;

    public PasswordResetData() {
        this(new MySQLDAO());
    }

    public PasswordResetData(MySQLDAO mysqlDAO) {
        this.mysqlDAO = Objects.requireNonNull(mysqlDAO, "Database access is required.");
    }

    @Override
    public void storeToken(Integer userId, String tokenHash, int validMinutes) throws StoreException {
        try {
            mysqlDAO.executeTransaction(
                    new String[] {
                        "DELETE FROM password_reset_tokens WHERE user_id=? AND used_at IS NULL",
                        "INSERT INTO password_reset_tokens (user_id, token_hash, expires_at, created_at) "
                                + "VALUES (?, ?, DATE_ADD(UTC_TIMESTAMP(), INTERVAL ? MINUTE), UTC_TIMESTAMP())"},
                    new Object[][] {{userId}, {userId, tokenHash, validMinutes}});
        } catch (Exception failure) {
            throw new StoreException("Unable to complete the password reset database operation.", failure);
        }
    }

    @Override
    public Integer redeemToken(String tokenHash) throws StoreException {
        try {
            // The conditional UPDATE is the single atomic step: only one caller can flip used_at from NULL.
            Integer changed = mysqlDAO.executeUpdate("UPDATE password_reset_tokens SET used_at=UTC_TIMESTAMP() "
                    + "WHERE token_hash=? AND used_at IS NULL AND expires_at > UTC_TIMESTAMP()", new Object[] {tokenHash});
            if (changed == null || changed != 1) {
                return null;
            }
            List<HashMap<String, Object>> rows = mysqlDAO.query(
                    "SELECT user_id AS userId FROM password_reset_tokens WHERE token_hash=?", new Object[] {tokenHash});
            return rows.isEmpty() ? null : Integer.valueOf(String.valueOf(rows.get(0).get("userId")));
        } catch (Exception failure) {
            throw new StoreException("Unable to complete the password reset database operation.", failure);
        }
    }
}

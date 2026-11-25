package br.uff.store.core.exceptions;

/** Sign-in was refused because the account must choose a new password by email first (decision 4A). */
public class PasswordResetRequiredException extends StoreException {
    public static final String MESSAGE = "This account needs a new password. We sent a reset link to its email address.";

    private final String email;

    public PasswordResetRequiredException(String email) {
        super(MESSAGE);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}

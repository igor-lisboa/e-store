package br.uff.store.core.services;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IPasswordResetData;
import br.uff.store.core.interfaces.data.IUserData;
import br.uff.store.core.interfaces.gateways.IEmailSender;
import br.uff.store.core.interfaces.services.IPasswordHasher;
import br.uff.store.core.interfaces.services.IPasswordResetService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Supplier;

/** Password recovery by email. Only a hash of each token is stored, so a database leak cannot reset accounts. */
public class PasswordResetService implements IPasswordResetService {
    static final int VALID_MINUTES = 60;
    private static final String SUBJECT = "Reset your e-store password";
    private static final String INVALID_LINK = "The reset link is invalid or has expired.";

    private final IUserData userData;
    private final IPasswordResetData resetData;
    private final IPasswordHasher passwordHasher;
    private final IEmailSender emailSender;
    private final Supplier<String> tokenGenerator;

    public PasswordResetService(IUserData userData, IPasswordResetData resetData, IPasswordHasher passwordHasher,
            IEmailSender emailSender, Supplier<String> tokenGenerator) {
        if (userData == null || resetData == null || passwordHasher == null || emailSender == null
                || tokenGenerator == null) {
            throw new IllegalArgumentException("Password reset dependencies are required.");
        }
        this.userData = userData;
        this.resetData = resetData;
        this.passwordHasher = passwordHasher;
        this.emailSender = emailSender;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public void requestReset(String email, String resetPageUrl) throws StoreException {
        if (email == null || !email.trim().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new StoreException("A valid email address is required.");
        }
        UserDTO user = userData.findUserByEmail(email.trim());
        if (user == null) {
            return; // Same outcome as for a known account, so the form cannot be used to discover accounts.
        }
        String token = tokenGenerator.get();
        resetData.storeToken(user.getId(), hash(token), VALID_MINUTES);
        String body = "Use this link within " + VALID_MINUTES + " minutes to choose a new password:\n"
                + resetPageUrl + "?token=" + token + "\n\nIf you did not ask for this, ignore this message.";
        try {
            emailSender.send(user.getEmail(), SUBJECT, body);
        } catch (StoreException failure) {
            throw new StoreException("Unable to send the reset email.", failure);
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) throws StoreException {
        // The password is checked first so a typo does not use up the one-time link.
        InputRules.newPassword(newPassword);
        if (token == null || token.trim().isEmpty()) {
            throw new StoreException(INVALID_LINK);
        }
        Integer userId = resetData.redeemToken(hash(token.trim()));
        if (userId == null) {
            throw new StoreException(INVALID_LINK);
        }
        userData.updatePassword(userId, passwordHasher.hash(newPassword));
    }

    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte value : digest) {
                hex.append(String.format("%02x", value));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException unavailable) {
            throw new IllegalStateException("SHA-256 is required by the Java platform.", unavailable);
        }
    }
}

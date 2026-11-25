package br.uff.store.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.uff.store.core.dtos.UserDTO;
import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.data.IPasswordResetData;
import br.uff.store.core.interfaces.data.IUserData;
import br.uff.store.core.interfaces.gateways.IEmailSender;
import br.uff.store.core.interfaces.services.IPasswordHasher;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class PasswordResetServiceTest {
    private static final String PAGE = "https://shop.example.test/password-reset";

    private final IUserData userData = mock(IUserData.class);
    private final IPasswordResetData resetData = mock(IPasswordResetData.class);
    private final IPasswordHasher hasher = mock(IPasswordHasher.class);
    private final IEmailSender email = mock(IEmailSender.class);
    private final PasswordResetService service = new PasswordResetService(userData, resetData, hasher, email, () -> "secret-token");

    @Test
    public void knownAccountGetsALinkAndOnlyTheTokenHashIsStored() throws Exception {
        when(userData.findUserByEmail("ana@example.test")).thenReturn(new UserDTO(7, "Ana", "ana@example.test", "x", 2));

        service.requestReset("  ana@example.test ", PAGE);

        ArgumentCaptor<String> stored = ArgumentCaptor.forClass(String.class);
        verify(resetData).storeToken(org.mockito.ArgumentMatchers.eq(7), stored.capture(), org.mockito.ArgumentMatchers.eq(60));
        assertEquals(PasswordResetService.hash("secret-token"), stored.getValue());
        assertFalse("the token itself must never be stored", stored.getValue().contains("secret-token"));
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(email).send(org.mockito.ArgumentMatchers.eq("ana@example.test"), anyString(), body.capture());
        assertTrue(body.getValue().contains(PAGE + "?token=secret-token"));
    }

    @Test
    public void unknownAccountLooksIdenticalAndSendsNothing() throws Exception {
        when(userData.findUserByEmail("nobody@example.test")).thenReturn(null);

        service.requestReset("nobody@example.test", PAGE);

        verifyNoInteractions(resetData, email);
    }

    @Test
    public void malformedEmailAddressesAreRejectedBeforeAnyLookup() throws Exception {
        for (String invalid : new String[] {null, "", "  ", "no-at-sign", "a@b", "a b@c.d"}) {
            try {
                service.requestReset(invalid, PAGE);
                fail("Accepted: " + invalid);
            } catch (StoreException expected) {
                assertEquals("A valid email address is required.", expected.getMessage());
            }
        }
        verifyNoInteractions(userData, resetData, email);
    }

    @Test
    public void aFailedEmailIsReportedWithoutLeakingTheTransportError() throws Exception {
        when(userData.findUserByEmail("ana@example.test")).thenReturn(new UserDTO(7, "Ana", "ana@example.test", "x", 2));
        doThrow(new StoreException("smtp://user:secret@mail.internal refused")).when(email).send(anyString(), anyString(), anyString());

        try {
            service.requestReset("ana@example.test", PAGE);
            fail("A failed send must be reported.");
        } catch (StoreException failure) {
            assertEquals("Unable to send the reset email.", failure.getMessage());
        }
    }

    @Test
    public void validTokenSetsTheNewPasswordHashed() throws Exception {
        when(resetData.redeemToken(PasswordResetService.hash("secret-token"))).thenReturn(7);
        when(hasher.hash("brand-new-password")).thenReturn("hashed-value");

        service.resetPassword(" secret-token ", "brand-new-password");

        verify(userData).updatePassword(7, "hashed-value");
    }

    @Test
    public void invalidExpiredOrUsedTokenChangesNoPassword() throws Exception {
        when(resetData.redeemToken(anyString())).thenReturn(null);

        for (String token : new String[] {"unknown", null, "", "  "}) {
            try {
                service.resetPassword(token, "brand-new-password");
                fail("Accepted token: " + token);
            } catch (StoreException expected) {
                assertEquals("The reset link is invalid or has expired.", expected.getMessage());
            }
        }
        verify(userData, never()).updatePassword(anyInt(), any());
    }

    @Test
    public void aTooShortPasswordIsRejectedWithoutUsingUpTheLink() throws Exception {
        try {
            service.resetPassword("secret-token", "short");
            fail("A short password must be rejected.");
        } catch (StoreException expected) {
            assertEquals("Password must be at least 8 characters.", expected.getMessage());
        }
        verify(resetData, never()).redeemToken(anyString());
    }

    @Test
    public void tokenHashIsTheStandardSha256Hex() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", PasswordResetService.hash("abc"));
    }

    @Test
    public void allDependenciesAreRequired() {
        try {
            new PasswordResetService(userData, resetData, hasher, email, null);
            fail("A token generator is required.");
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }
}

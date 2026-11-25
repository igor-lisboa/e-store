package br.uff.store.infrastructure.gateways;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import br.uff.store.core.exceptions.StoreException;
import br.uff.store.infrastructure.security.SecureTokenGenerator;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileEmailOutboxTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void eachMessageBecomesItsOwnReadableFile() throws Exception {
        Path outbox = folder.getRoot().toPath().resolve("mail");
        FileEmailOutbox sender = new FileEmailOutbox(outbox);

        sender.send("ana@example.test", "Hello", "Line one\nhttp://link?token=abc");
        sender.send("bob@example.test", "Hello", "Second");

        File[] files = outbox.toFile().listFiles();
        assertEquals("two messages must not overwrite each other", 2, files.length);
        String all = "";
        for (File file : files) {
            all += new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        }
        assertTrue(all.contains("To: ana@example.test\nSubject: Hello\n\nLine one\nhttp://link?token=abc"));
        assertTrue(all.contains("To: bob@example.test"));
    }

    @Test
    public void anUnwritableOutboxIsReportedAsAStoreException() throws Exception {
        File notADirectory = folder.newFile("blocker");
        FileEmailOutbox sender = new FileEmailOutbox(notADirectory.toPath().resolve("mail"));

        try {
            sender.send("ana@example.test", "Hello", "Body");
            fail("Writing below a regular file must fail.");
        } catch (StoreException failure) {
            assertEquals("Unable to write the demo email.", failure.getMessage());
        }
    }

    @Test
    public void tokensAreLongRandomAndSafeInAnAddress() {
        SecureTokenGenerator generator = new SecureTokenGenerator();
        Set<String> seen = new HashSet<>();
        for (int index = 0; index < 200; index++) {
            String token = generator.get();
            assertEquals("32 bytes encode to 43 URL-safe characters", 43, token.length());
            assertTrue(token, token.matches("[A-Za-z0-9_-]+"));
            seen.add(token);
        }
        assertEquals("tokens must not repeat", 200, seen.size());
    }
}

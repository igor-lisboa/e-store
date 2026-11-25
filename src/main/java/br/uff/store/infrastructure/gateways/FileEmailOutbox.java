package br.uff.store.infrastructure.gateways;

import br.uff.store.core.exceptions.StoreException;
import br.uff.store.core.interfaces.gateways.IEmailSender;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * NOT PRODUCTION READY: demo email adapter. It sends nothing; each message is written as a text file in an
 * outbox folder (EMAIL_OUTBOX_DIR, default: the system temp folder) so a developer can open the link. Replace it
 * with an adapter for a real provider by implementing {@link IEmailSender} and changing StoreServices only.
 */
public class FileEmailOutbox implements IEmailSender {
    private static final Logger LOGGER = Logger.getLogger(FileEmailOutbox.class.getName());

    private final Path directory;

    public FileEmailOutbox() {
        this(Paths.get(configuredDirectory()));
    }

    public FileEmailOutbox(Path directory) {
        this.directory = directory;
    }

    @Override
    public void send(String to, String subject, String body) throws StoreException {
        try {
            Files.createDirectories(directory);
            Path file = directory.resolve(System.currentTimeMillis() + "-" + UUID.randomUUID() + ".txt");
            String message = "To: " + to + "\nSubject: " + subject + "\n\n" + body + "\n";
            Files.write(file, message.getBytes(StandardCharsets.UTF_8));
            // The body holds a secret link, so only the file name is logged.
            LOGGER.info("Demo email for a recipient was written to " + file);
        } catch (IOException failure) {
            throw new StoreException("Unable to write the demo email.", failure);
        }
    }

    private static String configuredDirectory() {
        String configured = System.getenv("EMAIL_OUTBOX_DIR");
        return configured == null || configured.trim().isEmpty()
                ? System.getProperty("java.io.tmpdir") + "/e-store-outbox" : configured.trim();
    }
}

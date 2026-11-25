package br.uff.store.infrastructure.database;

import java.net.URI;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

/** Shared safety boundary for tests that deliberately reset a database. */
public final class MySqlTestDatabase {
    private MySqlTestDatabase() { }

    public static Flyway reset() {
        Flyway flyway = configure(null);
        flyway.clean();
        flyway.migrate();
        return flyway;
    }

    /** Cleans the schema and applies migrations only up to the target; the returned Flyway applies the rest. */
    public static Flyway resetTo(String targetVersion) {
        Flyway partial = configure(targetVersion);
        partial.clean();
        partial.migrate();
        return configure(null);
    }

    private static Flyway configure(String targetVersion) {
        String url = System.getenv("TEST_MYSQL_URL");
        if (url == null || !url.startsWith("jdbc:mysql:")
                || !"/e_store_integration".equals(URI.create(url.substring(5)).getPath())) {
            throw new IllegalStateException("TEST_MYSQL_URL must point to disposable database e_store_integration.");
        }
        FluentConfiguration configuration = Flyway.configure()
                .dataSource(url, System.getenv("TEST_MYSQL_USER"), System.getenv("TEST_MYSQL_PASSWORD"))
                .locations("filesystem:database/migrations", "filesystem:database/dev-seeds")
                .cleanDisabled(false);
        if (targetVersion != null) {
            configuration.target(targetVersion);
        }
        return configuration.load();
    }
}

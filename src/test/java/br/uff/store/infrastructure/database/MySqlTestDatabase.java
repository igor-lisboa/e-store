package br.uff.store.infrastructure.database;

import java.net.URI;
import org.flywaydb.core.Flyway;

/** Shared safety boundary for tests that deliberately reset a database. */
public final class MySqlTestDatabase {
    private MySqlTestDatabase() { }

    public static Flyway reset() {
        String url = System.getenv("TEST_MYSQL_URL");
        if (url == null || !url.startsWith("jdbc:mysql:")
                || !"/e_store_integration".equals(URI.create(url.substring(5)).getPath())) {
            throw new IllegalStateException("TEST_MYSQL_URL must point to disposable database e_store_integration.");
        }
        Flyway flyway = Flyway.configure().dataSource(url, System.getenv("TEST_MYSQL_USER"),
                System.getenv("TEST_MYSQL_PASSWORD"))
                .locations("filesystem:database/migrations", "filesystem:database/dev-seeds")
                .cleanDisabled(false).load();
        flyway.clean();
        flyway.migrate();
        return flyway;
    }
}

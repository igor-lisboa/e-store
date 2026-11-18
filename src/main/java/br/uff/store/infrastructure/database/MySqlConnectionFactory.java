package br.uff.store.infrastructure.database;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class MySqlConnectionFactory implements ConnectionFactory {
    @Override
    public Connection open() throws SQLException {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String url = setting(dotenv, "MYSQL_CONNECTION_STRING",
                "jdbc:mysql://localhost:3306/e_store?useSSL=false&serverTimezone=UTC");
        return DriverManager.getConnection(url,
                setting(dotenv, "MYSQL_USER", "e_store"), setting(dotenv, "MYSQL_PASSWORD", "123456"));
    }

    private String setting(Dotenv dotenv, String name, String fallback) {
        String environmentValue = System.getenv(name);
        if (environmentValue != null && !environmentValue.trim().isEmpty()) {
            return environmentValue;
        }
        String fileValue = dotenv.get(name);
        return fileValue == null || fileValue.trim().isEmpty() ? fallback : fileValue;
    }
}

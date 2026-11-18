package br.uff.store.infrastructure.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Each operation owns its resources; servlet requests never share a JDBC connection. */
public class MySQLDAO {
    private final ConnectionFactory connections;

    public MySQLDAO() {
        this(new MySqlConnectionFactory());
    }

    public MySQLDAO(ConnectionFactory connections) {
        this.connections = Objects.requireNonNull(connections, "Connection factory is required.");
    }

    public void executeTransaction(String[] commands, Object[][] bindings) throws SQLException {
        Objects.requireNonNull(commands, "Transaction commands are required.");
        try (Connection connection = connections.open()) {
            connection.setAutoCommit(false);
            try {
                for (int index = 0; index < commands.length; index++) {
                    Object[] parameters = bindings != null && index < bindings.length ? bindings[index] : null;
                    execute(connection, commands[index], parameters);
                }
                connection.commit();
            } catch (SQLException | RuntimeException failure) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
                throw failure;
            }
        }
    }

    public Integer executeUpdate(String command, Object[] bindings) throws SQLException {
        try (Connection connection = connections.open()) {
            return execute(connection, command, bindings);
        }
    }

    private int execute(Connection connection, String command, Object[] bindings) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(command)) {
            bind(statement, bindings);
            return statement.executeUpdate();
        }
    }

    public Object scalar(String field, String table, String filter, Object[] bindings) throws SQLException {
        if (!table.toUpperCase(Locale.ROOT).startsWith("SELECT")) {
            table = "SELECT * FROM " + table;
        }
        if (!filter.isEmpty()) {
            table = "SELECT * FROM (" + table + ") source WHERE " + filter;
        }
        List<HashMap<String, Object>> rows = query("SELECT " + field + " FROM (" + table + ") result", bindings);
        return rows.isEmpty() ? null : rows.get(0).values().iterator().next();
    }

    public List<HashMap<String, Object>> query(String command, Object[] bindings) throws SQLException {
        try (Connection connection = connections.open();
                PreparedStatement statement = connection.prepareStatement(command)) {
            bind(statement, bindings);
            try (ResultSet results = statement.executeQuery()) {
                ResultSetMetaData metadata = results.getMetaData();
                List<HashMap<String, Object>> rows = new ArrayList<>();
                while (results.next()) {
                    HashMap<String, Object> row = new HashMap<>();
                    for (int column = 1; column <= metadata.getColumnCount(); column++) {
                        row.put(metadata.getColumnLabel(column), results.getString(column));
                    }
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    private void bind(PreparedStatement statement, Object[] bindings) throws SQLException {
        if (bindings == null) {
            return;
        }
        for (int index = 0; index < bindings.length; index++) {
            statement.setObject(index + 1, bindings[index]);
        }
    }
}

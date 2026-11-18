package br.uff.store.infrastructure.database;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.Test;

public class JdbcCleanupTest {
    @Test
    public void queryFailureClosesStatementAndConnection() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        SQLException original = new SQLException("query failed");
        when(connection.prepareStatement("SELECT broken")).thenReturn(statement);
        when(statement.executeQuery()).thenThrow(original);

        try {
            new MySQLDAO(() -> connection).query("SELECT broken", null);
            fail("The database failure must reach the caller.");
        } catch (SQLException failure) {
            assertSame(original, failure);
            verify(statement).close();
            verify(connection).close();
        }
    }

    @Test
    public void rollbackFailureDoesNotReplaceOriginalSqlFailure() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        SQLException original = new SQLException("write failed");
        SQLException rollback = new SQLException("rollback failed");
        when(connection.prepareStatement("UPDATE broken")).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(original);
        doThrow(rollback).when(connection).rollback();

        try {
            new MySQLDAO(() -> connection).executeTransaction(new String[] {"UPDATE broken"}, null);
            fail("The write failure must reach the caller.");
        } catch (SQLException failure) {
            assertSame(original, failure);
            assertSame(rollback, failure.getSuppressed()[0]);
            verify(connection).close();
        }
    }
}

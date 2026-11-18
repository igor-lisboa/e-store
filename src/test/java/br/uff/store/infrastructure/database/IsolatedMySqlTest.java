package br.uff.store.infrastructure.database;

import org.junit.Before;

public abstract class IsolatedMySqlTest {
    @Before
    public void resetDatabaseBeforeEachTest() {
        MySqlTestDatabase.reset();
    }
}

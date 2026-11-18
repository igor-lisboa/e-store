package br.uff.store.steps.stepDefinition;

import br.uff.store.infrastructure.database.MySqlTestDatabase;
import io.cucumber.java.Before;

public class DatabaseHooks {
    @Before
    public void resetScenarioFixtures() {
        MySqlTestDatabase.reset();
    }
}

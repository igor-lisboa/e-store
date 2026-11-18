package br.uff.store.core.services;

import static org.junit.Assert.fail;
import br.uff.store.core.exceptions.StoreException;
import org.junit.Test;

public class InputRulesTest {
    @Test
    public void invalidAndOverflowingPageRequestsAreRejected() throws Exception {
        Integer[][] invalid = {{null, 1}, {0, 1}, {101, 1}, {5, null}, {5, 0}, {100, Integer.MAX_VALUE}};
        for (Integer[] page : invalid) {
            try {
                InputRules.page(page[0], page[1]);
                fail("Invalid paging must be rejected before generating SQL.");
            } catch (StoreException expected) {
                // The boundary is the exception: callers must not calculate an invalid SQL offset.
            }
        }
        InputRules.page(1, 1);
        InputRules.page(100, 2);
    }
}

package br.uff.store.steps.stepDefinition;

import br.uff.store.application.StoreServices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import br.uff.store.core.dtos.CartDTO;
import br.uff.store.core.services.CartService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class CartStepDefinition {
    private final CartService carts = StoreServices.carts();
    private Integer cartId;
    private Integer userId;
    private String ip;
    private CartDTO result;

    @Given("cart reference {int}, customer {int}, and visitor address {string}")
    public void cartReference(Integer cart, Integer customer, String address) {
        cartId = cart == 0 ? null : cart;
        userId = customer == 0 ? null : customer;
        ip = address;
    }

    @When("the visitor requests an active cart")
    public void requestCart() throws Exception {
        result = carts.getActiveCart(cartId, userId, ip);
    }

    @Then("the returned cart belongs to customer {int}")
    public void verifyOwner(Integer customer) {
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(customer == 0 ? null : customer, result.getUserId());
    }

    @When("customer {int} resumes the anonymous cart")
    public void claimAnonymousCart(Integer customer) throws Exception {
        Integer anonymousCartId = result.getId();
        result = carts.getActiveCart(anonymousCartId, customer, ip);
        assertEquals("Sign-in must retain the visitor's cart.", anonymousCartId, result.getId());
    }
}

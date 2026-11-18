package br.uff.store.steps.stepDefinition;

import br.uff.store.application.StoreServices;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.enums.ProductCategory;
import br.uff.store.core.services.ProductService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class ProductStepDefinition {
    private final ProductService products = StoreServices.products();
    private ProductDTO input;
    private Integer productId;

    @Given("a product named {string} priced at {double} with category {string}")
    public void productDetails(String name, Double price, String category) {
        input = new ProductDTO(null, name, price, "Scenario product", "img/ps4.jpg",
                ProductCategory.valueOf(category).getId(), 0);
    }

    @When("the administrator creates the product and adds {int} units of stock")
    public void createProductAndStock(Integer quantity) throws Exception {
        products.insertProduct(input);
        ProductDTO saved = products.listAdminProducts().stream()
                .filter(product -> input.getName().equals(product.getName())).findFirst()
                .orElseThrow(() -> new AssertionError("The created product must be persisted."));
        productId = saved.getId();
        products.addProductStock(productId, quantity);
    }

    @Then("the product has {int} units available and retains its submitted details")
    public void verifyPersistedProduct(Integer quantity) throws Exception {
        ProductDTO saved = products.findProductById(productId);
        assertEquals(quantity, saved.getQuantity());
        assertEquals(input.getName(), saved.getName());
        assertEquals(input.getPrice(), saved.getPrice());
        assertEquals(input.getCategoryId(), saved.getCategoryId());
        assertTrue(saved.getId() > 0);
    }
}

package br.uff.store.application.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import br.uff.store.core.dtos.CategoryDTO;
import br.uff.store.core.dtos.ProductDTO;
import br.uff.store.core.dtos.UserDTO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Test;

public class ApiSerializationTest {
    private final Gson gson = new Gson();

    @Test
    public void productResponseUsesEnglishPropertyNames() {
        ProductDTO product = new ProductDTO(4, "Console", 299.0, "Game console", "img/ps4.jpg", 5, 10);
        product.setCategory("Consoles");

        JsonObject json = gson.toJsonTree(product).getAsJsonObject();

        assertEquals(8, json.size());
        assertEquals("Console", json.get("name").getAsString());
        assertEquals(299.0, json.get("price").getAsDouble(), 0.001);
        assertEquals("Game console", json.get("description").getAsString());
        assertEquals("img/ps4.jpg", json.get("image").getAsString());
        assertEquals(5, json.get("categoryId").getAsInt());
        assertEquals(10, json.get("quantity").getAsInt());
        assertEquals("Consoles", json.get("category").getAsString());
        assertFalse(json.has("nome"));
    }

    @Test
    public void accountResponseExcludesCredentialsAndUsesEnglishProperties() {
        UserDTO user = new UserDTO(3, "Customer", "customer@example.test", "secret-hash", 2);

        JsonObject json = gson.toJsonTree(user).getAsJsonObject();

        assertEquals(4, json.size());
        assertEquals("Customer", json.get("name").getAsString());
        assertEquals(2, json.get("roleId").getAsInt());
        assertFalse(json.has("senha"));
        assertFalse(json.has("password"));
        assertFalse(gson.toJson(user).contains("secret-hash"));
    }

    @Test
    public void categoryResponseUsesEnglishPropertyNames() {
        CategoryDTO category = new CategoryDTO();
        category.setId(5);
        category.setNome("Consoles");

        JsonObject json = gson.toJsonTree(category).getAsJsonObject();

        assertEquals(2, json.size());
        assertEquals("Consoles", json.get("name").getAsString());
        assertFalse(json.has("nome"));
    }
}

package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ro.unibuc.prodeng.IntegrationTestBase;
import ro.unibuc.prodeng.repository.InventoryItemRepository;
import ro.unibuc.prodeng.repository.MenuItemRepository;
import ro.unibuc.prodeng.repository.SupplierRepository;
import ro.unibuc.prodeng.request.CreateInventoryItemRequest;
import ro.unibuc.prodeng.request.CreateMenuItemRequest;
import ro.unibuc.prodeng.request.CreateSupplierRequest;
import ro.unibuc.prodeng.request.IngredientRequirementRequest;
import ro.unibuc.prodeng.request.UpdateMenuItemRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("MenuController Integration Tests")
class MenuControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        menuItemRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        supplierRepository.deleteAll();
    }

    @Test
    void testCreateUpdateAndDeleteMenuItem_recipeAvailabilityIsCalculatedFromInventory() throws Exception {
        String supplierId = createSupplier(
                "Teo Pantry",
                "Daily Produce",
                "teo.pantry@example.com",
                "+40777777777");
        String doughId = createInventoryItem(
                "Pizza Dough",
                "portion",
                new BigDecimal("4.0"),
                new BigDecimal("1.0"),
                supplierId);
        String cheeseId = createInventoryItem(
                "Mozzarella",
                "kg",
                new BigDecimal("2.0"),
                new BigDecimal("0.5"),
                supplierId);

        String menuItemId = createMenuItem(
                "Margherita",
                "Classic pizza",
                new BigDecimal("38.0"),
                List.of(
                        new IngredientRequirementRequest(doughId, new BigDecimal("1.0")),
                        new IngredientRequirementRequest(cheeseId, new BigDecimal("1.5"))));

        mockMvc.perform(get("/api/menu-items/{id}", menuItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Margherita"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.recipe.length()").value(2));

        mockMvc.perform(put("/api/menu-items/{id}", menuItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateMenuItemRequest(
                                "Margherita XL",
                                "Large classic pizza",
                                new BigDecimal("45.0"),
                                List.of(
                                        new IngredientRequirementRequest(doughId, new BigDecimal("1.0")),
                                        new IngredientRequirementRequest(cheeseId, new BigDecimal("3.0")))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Margherita XL"))
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.price").value(45.0));

        mockMvc.perform(get("/api/menu-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].available").value(false));

        mockMvc.perform(delete("/api/menu-items/{id}", menuItemId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/menu-items/{id}", menuItemId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testCreateMenuItem_withMissingInventoryIngredient_returnsNotFound() throws Exception {
        CreateMenuItemRequest request = new CreateMenuItemRequest(
                "Ghost Burger",
                "Should not be persisted",
                new BigDecimal("25.0"),
                List.of(new IngredientRequirementRequest("missing-ingredient-id", new BigDecimal("1.0"))));

        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Entity: missing-ingredient-id was not found"));
    }

    private String createSupplier(String name, String companyName, String email, String phoneNumber)
            throws Exception {
        CreateSupplierRequest request = new CreateSupplierRequest(name, companyName, email, phoneNumber);

        String response = mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private String createInventoryItem(
            String name,
            String unit,
            BigDecimal quantityInStock,
            BigDecimal reorderThreshold,
            String supplierId) throws Exception {
        CreateInventoryItemRequest request = new CreateInventoryItemRequest(
                name,
                unit,
                quantityInStock,
                reorderThreshold,
                supplierId);

        String response = mockMvc.perform(post("/api/inventory-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private String createMenuItem(
            String name,
            String description,
            BigDecimal price,
            List<IngredientRequirementRequest> recipe) throws Exception {
        CreateMenuItemRequest request = new CreateMenuItemRequest(name, description, price, recipe);

        String response = mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }
}

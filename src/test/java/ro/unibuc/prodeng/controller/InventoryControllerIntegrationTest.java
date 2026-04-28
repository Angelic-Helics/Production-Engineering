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
import ro.unibuc.prodeng.request.RestockInventoryItemRequest;
import ro.unibuc.prodeng.request.UpdateInventoryItemRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("InventoryController Integration Tests")
class InventoryControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

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
    void testCreateFilterRestockUpdateAndDeleteInventoryItem_fullFlowWorks() throws Exception {
        String primarySupplierId = createSupplier(
                "Luca Stock",
                "Primary Pantry",
                "luca.stock@example.com",
                "+40444444444");
        String secondarySupplierId = createSupplier(
                "Ioana Stock",
                "Reserve Pantry",
                "ioana.stock@example.com",
                "+40555555555");

        String flourId = createInventoryItem(
                "Flour",
                "KG",
                new BigDecimal("5.0"),
                new BigDecimal("5.0"),
                primarySupplierId);
        createInventoryItem(
                "Tomatoes",
                "KG",
                new BigDecimal("20.0"),
                new BigDecimal("4.0"),
                secondarySupplierId);

        mockMvc.perform(get("/api/inventory-items").param("lowStock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Flour"))
                .andExpect(jsonPath("$[0].lowStock").value(true));

        mockMvc.perform(patch("/api/inventory-items/{id}/restock", flourId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RestockInventoryItemRequest(new BigDecimal("7.5")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityInStock").value(12.5))
                .andExpect(jsonPath("$.lowStock").value(false));

        mockMvc.perform(put("/api/inventory-items/{id}", flourId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateInventoryItemRequest(
                                "Fine Flour",
                                " grams ",
                                new BigDecimal("3.0"),
                                secondarySupplierId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Fine Flour"))
                .andExpect(jsonPath("$.unit").value("grams"))
                .andExpect(jsonPath("$.supplierId").value(secondarySupplierId));

        mockMvc.perform(get("/api/inventory-items").param("supplierId", secondarySupplierId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(delete("/api/inventory-items/{id}", flourId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/inventory-items/{id}", flourId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testDeleteInventoryItem_usedByMenu_returnsBadRequest() throws Exception {
        String supplierId = createSupplier(
                "Daria Supply",
                "Kitchen Depot",
                "daria.supply@example.com",
                "+40666666666");
        String cheeseId = createInventoryItem(
                "Cheese",
                "kg",
                new BigDecimal("10.0"),
                new BigDecimal("2.0"),
                supplierId);

        CreateMenuItemRequest menuRequest = new CreateMenuItemRequest(
                "Cheese Plate",
                "Assorted cheeses",
                new BigDecimal("32.5"),
                List.of(new IngredientRequirementRequest(cheeseId, new BigDecimal("0.5"))));

        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(menuRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/inventory-items/{id}", cheeseId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Inventory item is used by at least one menu item: " + cheeseId));
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
}

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
import ro.unibuc.prodeng.repository.SupplierRepository;
import ro.unibuc.prodeng.request.CreateInventoryItemRequest;
import ro.unibuc.prodeng.request.CreateSupplierRequest;
import ro.unibuc.prodeng.request.UpdateSupplierRequest;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("SupplierController Integration Tests")
class SupplierControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        inventoryItemRepository.deleteAll();
        supplierRepository.deleteAll();
    }

    @Test
    void testCreateUpdateAndDeleteSupplier_existingSupplier_managesLifecycleSuccessfully() throws Exception {
        String supplierId = createSupplier(
                "Alex Supplier",
                "Fresh Farms",
                "alex.supplier@example.com",
                "+40111111111");

        mockMvc.perform(get("/api/suppliers/{id}", supplierId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Fresh Farms"));

        UpdateSupplierRequest updateRequest = new UpdateSupplierRequest(
                "Alex Updated",
                "Fresh Farms SRL",
                "alex.updated@example.com",
                "+40222222222");

        mockMvc.perform(put("/api/suppliers/{id}", supplierId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alex Updated"))
                .andExpect(jsonPath("$.email").value("alex.updated@example.com"));

        mockMvc.perform(delete("/api/suppliers/{id}", supplierId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/suppliers/{id}", supplierId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testDeleteSupplier_withAssignedInventory_returnsBadRequest() throws Exception {
        String supplierId = createSupplier(
                "Mara Supplier",
                "Pantry Hub",
                "mara.supplier@example.com",
                "+40333333333");

        CreateInventoryItemRequest inventoryRequest = new CreateInventoryItemRequest(
                "Olive Oil",
                "Liters",
                new BigDecimal("15.0"),
                new BigDecimal("5.0"),
                supplierId);

        mockMvc.perform(post("/api/inventory-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventoryRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/suppliers/{id}", supplierId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Supplier is still assigned to inventory items: " + supplierId));
    }

    private String createSupplier(String name, String companyName, String email, String phoneNumber)
            throws Exception {
        CreateSupplierRequest request = new CreateSupplierRequest(name, companyName, email, phoneNumber);

        String response = mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }
}

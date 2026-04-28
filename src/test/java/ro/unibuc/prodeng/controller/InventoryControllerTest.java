package ro.unibuc.prodeng.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.prodeng.exception.GlobalExceptionHandler;
import ro.unibuc.prodeng.request.CreateInventoryItemRequest;
import ro.unibuc.prodeng.request.RestockInventoryItemRequest;
import ro.unibuc.prodeng.response.InventoryItemResponse;
import ro.unibuc.prodeng.service.InventoryService;

@ExtendWith(SpringExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetInventoryItems_returnsFilteredList() throws Exception {
        List<InventoryItemResponse> responses = List.of(
                new InventoryItemResponse("1", "Mozzarella", "kg", new BigDecimal("1.50"),
                        new BigDecimal("2.00"), true, "sup-1", "Fresh Farm Supply"));
        when(inventoryService.getInventoryItems(true, "sup-1")).thenReturn(responses);

        mockMvc.perform(get("/api/inventory-items")
                        .param("lowStock", "true")
                        .param("supplierId", "sup-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].lowStock", is(true)))
                .andExpect(jsonPath("$[0].supplierName", is("Fresh Farm Supply")));

        verify(inventoryService, times(1)).getInventoryItems(true, "sup-1");
    }

    @Test
    void testCreateInventoryItem_validRequest_createsItem() throws Exception {
        CreateInventoryItemRequest request = new CreateInventoryItemRequest(
                "Mozzarella",
                "kg",
                new BigDecimal("4.50"),
                new BigDecimal("2.00"),
                "sup-1"
        );
        InventoryItemResponse response = new InventoryItemResponse(
                "1",
                "Mozzarella",
                "kg",
                new BigDecimal("4.50"),
                new BigDecimal("2.00"),
                false,
                "sup-1",
                "Fresh Farm Supply"
        );
        when(inventoryService.createInventoryItem(any(CreateInventoryItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/inventory-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantityInStock", is(4.50)))
                .andExpect(jsonPath("$.lowStock", is(false)));

        verify(inventoryService, times(1)).createInventoryItem(any(CreateInventoryItemRequest.class));
    }

    @Test
    void testRestockInventoryItem_existingItem_updatesQuantity() throws Exception {
        RestockInventoryItemRequest request = new RestockInventoryItemRequest(new BigDecimal("3.00"));
        InventoryItemResponse response = new InventoryItemResponse(
                "1",
                "Mozzarella",
                "kg",
                new BigDecimal("7.50"),
                new BigDecimal("2.00"),
                false,
                "sup-1",
                "Fresh Farm Supply"
        );
        when(inventoryService.restockInventoryItem(eq("1"), any(RestockInventoryItemRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/inventory-items/{id}/restock", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantityInStock", is(7.50)));

        verify(inventoryService, times(1)).restockInventoryItem(eq("1"), any(RestockInventoryItemRequest.class));
    }
}

package ro.unibuc.prodeng.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import ro.unibuc.prodeng.request.CreateMenuItemRequest;
import ro.unibuc.prodeng.request.IngredientRequirementRequest;
import ro.unibuc.prodeng.request.UpdateMenuItemRequest;
import ro.unibuc.prodeng.response.IngredientRequirementResponse;
import ro.unibuc.prodeng.response.MenuItemResponse;
import ro.unibuc.prodeng.service.MenuService;

@ExtendWith(SpringExtension.class)
class MenuControllerTest {

    @Mock
    private MenuService menuService;

    @InjectMocks
    private MenuController menuController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(menuController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetMenuItemById_existingMenuItem_returnsMenuItem() throws Exception {
        MenuItemResponse response = new MenuItemResponse(
                "1",
                "Margherita Pizza",
                "Classic pizza with mozzarella",
                new BigDecimal("39.99"),
                true,
                List.of(new IngredientRequirementResponse("inv-1", "Mozzarella", new BigDecimal("0.30")))
        );
        when(menuService.getMenuItemById("1")).thenReturn(response);

        mockMvc.perform(get("/api/menu-items/{id}", "1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Margherita Pizza")))
                .andExpect(jsonPath("$.available", is(true)));

        verify(menuService, times(1)).getMenuItemById("1");
    }

    @Test
    void testCreateMenuItem_validRequest_createsMenuItem() throws Exception {
        CreateMenuItemRequest request = new CreateMenuItemRequest(
                "Margherita Pizza",
                "Classic pizza with mozzarella",
                new BigDecimal("39.99"),
                List.of(new IngredientRequirementRequest("inv-1", new BigDecimal("0.30")))
        );
        MenuItemResponse response = new MenuItemResponse(
                "1",
                "Margherita Pizza",
                "Classic pizza with mozzarella",
                new BigDecimal("39.99"),
                true,
                List.of(new IngredientRequirementResponse("inv-1", "Mozzarella", new BigDecimal("0.30")))
        );
        when(menuService.createMenuItem(any(CreateMenuItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/menu-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price", is(39.99)))
                .andExpect(jsonPath("$.recipe[0].inventoryItemName", is("Mozzarella")));

        verify(menuService, times(1)).createMenuItem(any(CreateMenuItemRequest.class));
    }

    @Test
    void testUpdateMenuItem_existingMenuItem_updatesMenuItem() throws Exception {
        UpdateMenuItemRequest request = new UpdateMenuItemRequest(
                "Margherita Pizza",
                "Classic pizza with extra mozzarella",
                new BigDecimal("42.50"),
                List.of(new IngredientRequirementRequest("inv-1", new BigDecimal("0.35")))
        );
        MenuItemResponse response = new MenuItemResponse(
                "1",
                "Margherita Pizza",
                "Classic pizza with extra mozzarella",
                new BigDecimal("42.50"),
                true,
                List.of(new IngredientRequirementResponse("inv-1", "Mozzarella", new BigDecimal("0.35")))
        );
        when(menuService.updateMenuItem(eq("1"), any(UpdateMenuItemRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/menu-items/{id}", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Classic pizza with extra mozzarella")))
                .andExpect(jsonPath("$.price", is(42.50)));

        verify(menuService, times(1)).updateMenuItem(eq("1"), any(UpdateMenuItemRequest.class));
    }
}

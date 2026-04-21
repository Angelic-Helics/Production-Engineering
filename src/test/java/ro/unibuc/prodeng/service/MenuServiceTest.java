package ro.unibuc.prodeng.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.IngredientRequirement;
import ro.unibuc.prodeng.model.InventoryItemEntity;
import ro.unibuc.prodeng.model.MenuItemEntity;
import ro.unibuc.prodeng.repository.InventoryItemRepository;
import ro.unibuc.prodeng.repository.MenuItemRepository;
import ro.unibuc.prodeng.request.CreateMenuItemRequest;
import ro.unibuc.prodeng.request.IngredientRequirementRequest;
import ro.unibuc.prodeng.response.MenuItemResponse;

@ExtendWith(SpringExtension.class)
class MenuServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    void testCreateMenuItem_validRecipe_createsAvailableItem() {
        CreateMenuItemRequest request = new CreateMenuItemRequest(
                "Margherita Pizza",
                "Classic pizza with mozzarella",
                new BigDecimal("39.99"),
                List.of(new IngredientRequirementRequest("inv-1", new BigDecimal("0.30")))
        );
        InventoryItemEntity mozzarella = new InventoryItemEntity("inv-1", "Mozzarella", "kg",
                new BigDecimal("2.00"), new BigDecimal("1.00"), "sup-1");

        when(inventoryItemRepository.findById("inv-1")).thenReturn(Optional.of(mozzarella));
        when(menuItemRepository.save(any(MenuItemEntity.class))).thenAnswer(invocation -> {
            MenuItemEntity entity = invocation.getArgument(0);
            return new MenuItemEntity("menu-1", entity.name(), entity.description(), entity.price(), entity.recipe());
        });

        MenuItemResponse result = menuService.createMenuItem(request);

        assertEquals("menu-1", result.id());
        assertEquals("Mozzarella", result.recipe().get(0).inventoryItemName());
        assertTrue(result.available());
    }

    @Test
    void testGetMenuItemById_insufficientStock_marksAsUnavailable() {
        MenuItemEntity item = new MenuItemEntity("menu-1", "Margherita Pizza", "Classic pizza",
                new BigDecimal("39.99"), List.of(new IngredientRequirement("inv-1", new BigDecimal("0.30"))));
        InventoryItemEntity mozzarella = new InventoryItemEntity("inv-1", "Mozzarella", "kg",
                new BigDecimal("0.10"), new BigDecimal("1.00"), "sup-1");

        when(menuItemRepository.findById("menu-1")).thenReturn(Optional.of(item));
        when(inventoryItemRepository.findById("inv-1")).thenReturn(Optional.of(mozzarella));

        MenuItemResponse result = menuService.getMenuItemById("menu-1");

        assertFalse(result.available());
    }

    @Test
    void testCreateMenuItem_missingInventoryItem_throwsEntityNotFoundException() {
        CreateMenuItemRequest request = new CreateMenuItemRequest(
                "Margherita Pizza",
                "Classic pizza with mozzarella",
                new BigDecimal("39.99"),
                List.of(new IngredientRequirementRequest("missing", new BigDecimal("0.30")))
        );

        when(inventoryItemRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> menuService.createMenuItem(request));
    }

    @Test
    void testDeleteMenuItem_missingItem_throwsEntityNotFoundException() {
        when(menuItemRepository.existsById("missing")).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> menuService.deleteMenuItem("missing"));
    }
}

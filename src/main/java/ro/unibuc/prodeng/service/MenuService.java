package ro.unibuc.prodeng.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.IngredientRequirement;
import ro.unibuc.prodeng.model.InventoryItemEntity;
import ro.unibuc.prodeng.model.MenuItemEntity;
import ro.unibuc.prodeng.repository.InventoryItemRepository;
import ro.unibuc.prodeng.repository.MenuItemRepository;
import ro.unibuc.prodeng.request.CreateMenuItemRequest;
import ro.unibuc.prodeng.request.IngredientRequirementRequest;
import ro.unibuc.prodeng.request.UpdateMenuItemRequest;
import ro.unibuc.prodeng.response.IngredientRequirementResponse;
import ro.unibuc.prodeng.response.MenuItemResponse;

@Service
public class MenuService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    public List<MenuItemResponse> getAllMenuItems() {
        return menuItemRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public MenuItemResponse getMenuItemById(String id) {
        return toResponse(getMenuItemEntityById(id));
    }

    public MenuItemResponse createMenuItem(CreateMenuItemRequest request) {
        List<IngredientRequirement> recipe = toRecipe(request.recipe());
        MenuItemEntity item = new MenuItemEntity(
                null,
                request.name(),
                request.description(),
                request.price(),
                recipe
        );

        return toResponse(menuItemRepository.save(item));
    }

    public MenuItemResponse updateMenuItem(String id, UpdateMenuItemRequest request) {
        MenuItemEntity existing = getMenuItemEntityById(id);
        List<IngredientRequirement> recipe = toRecipe(request.recipe());
        MenuItemEntity updated = new MenuItemEntity(
                existing.id(),
                request.name(),
                request.description(),
                request.price(),
                recipe
        );

        return toResponse(menuItemRepository.save(updated));
    }

    public void deleteMenuItem(String id) {
        if (!menuItemRepository.existsById(id)) {
            throw new EntityNotFoundException(id);
        }

        menuItemRepository.deleteById(id);
    }

    private MenuItemEntity getMenuItemEntityById(String id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
    }

    private List<IngredientRequirement> toRecipe(List<IngredientRequirementRequest> requests) {
        return requests.stream()
                .map(request -> {
                    inventoryItemRepository.findById(request.inventoryItemId())
                            .orElseThrow(() -> new EntityNotFoundException(request.inventoryItemId()));

                    return new IngredientRequirement(
                            request.inventoryItemId(),
                            request.quantityRequired());
                })
                .toList();
    }

    private boolean isAvailable(MenuItemEntity item) {
        return item.recipe().stream().allMatch(requirement -> {
            InventoryItemEntity inventoryItem = inventoryItemRepository.findById(requirement.inventoryItemId())
                    .orElseThrow(() -> new EntityNotFoundException(requirement.inventoryItemId()));

            return inventoryItem.quantityInStock().compareTo(requirement.quantityRequired()) >= 0;
        });
    }

    private MenuItemResponse toResponse(MenuItemEntity item) {
        return new MenuItemResponse(
                item.id(),
                item.name(),
                item.description(),
                item.price(),
                isAvailable(item),
                item.recipe().stream()
                        .map(requirement -> {
                            InventoryItemEntity inventoryItem = inventoryItemRepository.findById(
                                            requirement.inventoryItemId())
                                    .orElseThrow(() -> new EntityNotFoundException(requirement.inventoryItemId()));

                            return new IngredientRequirementResponse(
                                    requirement.inventoryItemId(),
                                    inventoryItem.name(),
                                    requirement.quantityRequired());
                        })
                        .toList()
        );
    }
}

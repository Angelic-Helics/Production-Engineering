package ro.unibuc.prodeng.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import ro.unibuc.prodeng.request.CreateInventoryItemRequest;
import ro.unibuc.prodeng.request.RestockInventoryItemRequest;
import ro.unibuc.prodeng.request.UpdateInventoryItemRequest;
import ro.unibuc.prodeng.response.InventoryItemResponse;
import ro.unibuc.prodeng.service.InventoryService;

@RestController
@RequestMapping("/api/inventory-items")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryItemResponse>> getInventoryItems(
            @RequestParam(required = false) Boolean lowStock,
            @RequestParam(required = false) String supplierId) {
        return ResponseEntity.ok(inventoryService.getInventoryItems(lowStock, supplierId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryItemResponse> getInventoryItemById(@PathVariable String id) {
        return ResponseEntity.ok(inventoryService.getInventoryItemById(id));
    }

    @PostMapping
    public ResponseEntity<InventoryItemResponse> createInventoryItem(
            @Valid @RequestBody CreateInventoryItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createInventoryItem(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryItemResponse> updateInventoryItem(
            @PathVariable String id,
            @Valid @RequestBody UpdateInventoryItemRequest request) {
        return ResponseEntity.ok(inventoryService.updateInventoryItem(id, request));
    }

    @PatchMapping("/{id}/restock")
    public ResponseEntity<InventoryItemResponse> restockInventoryItem(
            @PathVariable String id,
            @Valid @RequestBody RestockInventoryItemRequest request) {
        return ResponseEntity.ok(inventoryService.restockInventoryItem(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable String id) {
        inventoryService.deleteInventoryItem(id);
        return ResponseEntity.noContent().build();
    }
}

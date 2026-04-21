package ro.unibuc.prodeng.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ro.unibuc.prodeng.model.InventoryItemEntity;

public interface InventoryItemRepository extends MongoRepository<InventoryItemEntity, String> {

    List<InventoryItemEntity> findBySupplierId(String supplierId);
}

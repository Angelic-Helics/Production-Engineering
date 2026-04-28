package ro.unibuc.prodeng.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import ro.unibuc.prodeng.model.MenuItemEntity;

public interface MenuItemRepository extends MongoRepository<MenuItemEntity, String> {
}

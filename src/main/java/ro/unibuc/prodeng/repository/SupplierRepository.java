package ro.unibuc.prodeng.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ro.unibuc.prodeng.model.SupplierEntity;

public interface SupplierRepository extends MongoRepository<SupplierEntity, String> {

    Optional<SupplierEntity> findByEmail(String email);
}

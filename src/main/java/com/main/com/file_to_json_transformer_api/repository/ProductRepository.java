package com.main.com.file_to_json_transformer_api.repository;

import com.main.com.file_to_json_transformer_api.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findById(String id);

    @Query("SELECT MAX(p.id) FROM Product p")
    Integer findMaxId();
}

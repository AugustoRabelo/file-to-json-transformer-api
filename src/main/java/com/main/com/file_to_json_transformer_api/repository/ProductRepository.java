package com.main.com.file_to_json_transformer_api.repository;

import com.main.com.file_to_json_transformer_api.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findById(String id);
}

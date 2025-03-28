package com.main.com.file_to_json_transformer_api.repository;

import com.main.com.file_to_json_transformer_api.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findById(String id);
}

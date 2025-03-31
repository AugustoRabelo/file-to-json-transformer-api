package com.main.com.file_to_json_transformer_api.repository;

import com.main.com.file_to_json_transformer_api.entity.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findById(String id);

    List<Order> findByDateBetween(LocalDate startDate, LocalDate endDate, Sort sort);
}

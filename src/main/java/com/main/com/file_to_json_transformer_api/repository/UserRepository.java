package com.main.com.file_to_json_transformer_api.repository;

import com.main.com.file_to_json_transformer_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(String id);
}

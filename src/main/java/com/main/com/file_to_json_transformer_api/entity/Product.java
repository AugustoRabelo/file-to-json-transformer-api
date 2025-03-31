package com.main.com.file_to_json_transformer_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name="products")
public class Product {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "product_value", nullable = false)
    private BigDecimal value;
}

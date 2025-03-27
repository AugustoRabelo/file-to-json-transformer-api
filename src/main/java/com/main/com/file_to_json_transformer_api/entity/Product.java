package com.main.com.file_to_json_transformer_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="products")
public class Product {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "product_value")
    private double value;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

}

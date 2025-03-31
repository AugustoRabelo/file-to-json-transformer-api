package com.main.com.file_to_json_transformer_api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductDTO {

    private String productId;

    private BigDecimal value;

    public ProductDTO(String productId, BigDecimal value) {
        this.productId = productId;
        this.value = value;
    }

}

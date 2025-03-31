package com.main.com.file_to_json_transformer_api.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class OrderDTO {

    private String orderId;

    private BigDecimal total;

    private LocalDate date;

    private List<ProductDTO> products;

}

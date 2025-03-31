package com.main.com.file_to_json_transformer_api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDTO {

    private String userId;

    private String name;

    private OrderDTO order;

}

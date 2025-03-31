package com.main.com.file_to_json_transformer_api.service;

import com.main.com.file_to_json_transformer_api.dto.OrderDTO;
import com.main.com.file_to_json_transformer_api.dto.ProductDTO;
import com.main.com.file_to_json_transformer_api.dto.UserDTO;
import com.main.com.file_to_json_transformer_api.entity.Order;
import com.main.com.file_to_json_transformer_api.entity.Product;
import com.main.com.file_to_json_transformer_api.entity.User;
import com.main.com.file_to_json_transformer_api.repository.OrderRepository;
import com.main.com.file_to_json_transformer_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataExportService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    public UserDTO getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        User user = order.getUser();

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getId());
        userDTO.setName(user.getName());

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(order.getId());
        orderDTO.setTotal(order.getTotal());
        orderDTO.setDate(LocalDate.parse(order.getDate().toString()));

        List<ProductDTO> productDTOs = order.getProducts().stream()
                .map(product -> {
                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setProductId(product.getId());
                    productDTO.setValue(product.getValue());
                    return productDTO;
                })
                .collect(Collectors.toList());

        orderDTO.setProducts(productDTOs);

        userDTO.setOrder(orderDTO);

        return userDTO;
    }

    public List<UserDTO> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        Sort sort = Sort.by(Sort.Order.asc("date"));
        List<Order> orders = orderRepository.findByDateBetween(startDate, endDate, sort);

        List<UserDTO> userDTOs = new ArrayList<>();
        for (Order order : orders) {
            UserDTO userDTO = new UserDTO();
            User user = order.getUser();

            userDTO.setUserId(user.getId());
            userDTO.setName(user.getName());

            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setOrderId(order.getId());
            orderDTO.setTotal(order.getTotal());
            orderDTO.setDate(order.getDate());

            List<ProductDTO> productDTOs = new ArrayList<>();
            for (Product product : order.getProducts()) {
                ProductDTO productDTO = new ProductDTO();
                productDTO.setProductId(product.getId());
                productDTO.setValue(product.getValue());
                productDTOs.add(productDTO);
            }
            orderDTO.setProducts(productDTOs);

            userDTO.setOrder(orderDTO);
            userDTOs.add(userDTO);
        }

        return userDTOs;
    }

}

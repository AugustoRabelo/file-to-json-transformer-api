package com.main.com.file_to_json_transformer_api.service;

import com.main.com.file_to_json_transformer_api.entity.Order;
import com.main.com.file_to_json_transformer_api.entity.Product;
import com.main.com.file_to_json_transformer_api.entity.User;
import com.main.com.file_to_json_transformer_api.repository.OrderRepository;
import com.main.com.file_to_json_transformer_api.repository.ProductRepository;
import com.main.com.file_to_json_transformer_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Service
public class FileImportService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    public void importFile(MultipartFile file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            processLine(line);
        }

        reader.close();
    }

    private void processLine(String line) {
        String userId = line.substring(0, 10).trim();
        String userName = line.substring(10, 55).trim();
        String orderId = line.substring(55, 65).trim();
        String productId = line.substring(65, 75).trim();
        BigDecimal productValue = new BigDecimal(line.substring(75, 87).trim());
        String dateString = line.substring(87, 95).trim();
        LocalDate purchaseDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));

        User user = userRepository.findById(userId).orElseGet(() -> {
            User newUser = new User();
            newUser.setId(userId);
            newUser.setName(userName);
            return newUser;
        });

        userRepository.save(user);

        Order order = orderRepository.findById(orderId).orElse(null);

        if (order == null) {
            order = new Order();
            order.setId(orderId);
            order.setDate(purchaseDate);
            order.setUser(user);

            orderRepository.save(order);
        }

        Order finalOrder = order;
        Product product = productRepository.findById(productId).orElseGet(() -> {
            Product newProduct = new Product();
            newProduct.setId(productId);
            newProduct.setValue(productValue);
            newProduct.setOrder(finalOrder);
            return newProduct;
        });

        productRepository.save(product);

        if (order.getProducts() == null) {
            order.setProducts(new ArrayList<>());
        }
        order.getProducts().add(product);

        orderRepository.save(order);
        }

}


package com.main.com.file_to_json_transformer_api.service;

import com.main.com.file_to_json_transformer_api.entity.Order;
import com.main.com.file_to_json_transformer_api.entity.Product;
import com.main.com.file_to_json_transformer_api.entity.User;
import com.main.com.file_to_json_transformer_api.repository.OrderRepository;
import com.main.com.file_to_json_transformer_api.repository.ProductRepository;
import com.main.com.file_to_json_transformer_api.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FileImportService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private static final int BATCH_SIZE = 1000;

    @Transactional
    public void importFile(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<String> lines = reader.lines().toList();
            if (lines.isEmpty()) {
                throw new FileImportException("File is empty.");
            }

            List<String> invalidLines = new ArrayList<>();
            int processedLines = 0;
            List<Order> ordersBatch = new ArrayList<>();

            for (String line : lines) {
                try {
                    validateLineFormat(line);
                    processLine(line, ordersBatch);
                    processedLines++;
                } catch (DataProcessingException e) {
                    logError("Error processing line", e);
                    invalidLines.add(line);
                } catch (Exception e) {
                    logError("Unexpected error processing line", e);
                    invalidLines.add(line);
                }

                if (ordersBatch.size() >= BATCH_SIZE) {
                    saveBatch(ordersBatch);
                    ordersBatch.clear();
                }
            }

            if (!ordersBatch.isEmpty()) {
                saveBatch(ordersBatch);
            }

            logFileImportSummary(processedLines, invalidLines.size());

            if (!invalidLines.isEmpty()) {
                throw new FileImportException("Some lines failed to process. Check logs for details.");
            }

        } catch (IOException e) {
            logError("Error reading file", e);
            throw new FileImportException("Failed to import file");
        }
    }

    private Order createOrderFromLine(String line, User user) {
        String orderId = line.substring(55, 65).trim();
        String productId = line.substring(65, 75).trim();
        BigDecimal productValue = new BigDecimal(line.substring(75, 87).trim());
        String dateString = line.substring(87, 95).trim();
        LocalDate purchaseDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));

        Order order = findOrCreateOrder(orderId, purchaseDate, user);
        Product product = findOrCreateProduct(productId, productValue);
        updateOrderWithProduct(order, product);

        return order;
    }

    @Transactional
    public void processLine(String line, List<Order> ordersBatch) {
        try {
            String userId = line.substring(0, 10).trim();
            String userName = line.substring(10, 55).trim();

            User user = findOrCreateUser(userId, userName);
            Order order = createOrderFromLine(line, user);

            ordersBatch.add(order);
        } catch (DataProcessingException e) {
            logError("Error processing data line", e);
            throw e;
        } catch (Exception e) {
            logError("Error processing line of data", e);
            throw new DataProcessingException("Error processing data from line");
        }
    }

    private void saveBatch(List<Order> ordersBatch) {
        log.info("Saving batch of {} orders", ordersBatch.size());
        orderRepository.saveAll(ordersBatch);
    }

    private void logFileImportSummary(int processedLines, int invalidLinesCount) {
        log.info("File processing summary:");
        log.info("Processed lines: {}", processedLines);
        log.info("Invalid lines: {}", invalidLinesCount);
    }

    private void validateLineFormat(String line) {
        if (line == null || line.length() < 95) {
            throw new DataProcessingException("Invalid line format: line too short");
        }
    }

    private User findOrCreateUser(String userId, String userName) {
        if (!userId.matches("\\d+")) {
            throw new DataProcessingException("Invalid userId format");
        }

        userId = userId.replaceFirst("^0+", "");

        String finalUserId = userId;
        return userRepository.findById(userId).orElseGet(() -> {
            try {
                User newUser = new User();
                newUser.setId(finalUserId);
                newUser.setName(userName);
                userRepository.save(newUser);
                return newUser;
            } catch (Exception e) {
                logError("Error creating user " + finalUserId, e);
                throw new DatabaseOperationException("Error saving user " + finalUserId + " to database");
            }
        });
    }

    private Order findOrCreateOrder(String orderId, LocalDate purchaseDate, User user) {
        if (!orderId.matches("\\d+")) {
            throw new DataProcessingException("Invalid orderId " + orderId  + " format: ");
        }

        orderId = orderId.replaceFirst("^0+", "");

        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            try {
                order = new Order();
                order.setId(orderId);
                order.setDate(purchaseDate);
                order.setUser(user);
                orderRepository.save(order);
            } catch (Exception e) {
                logError("Error creating order: " + orderId, e);
                throw new DatabaseOperationException("Error saving order " + order + " to database");
            }
        }
        return order;
    }

    private Product findOrCreateProduct(String productId, BigDecimal productValue) {
        if (isInvalidProductId(productId)) {
            productId = getNextProductId();  // Gerar um novo ID único
        }

        if (!productId.matches("\\d+")) {
            throw new DataProcessingException("Invalid productId " + productId + " format: ");
        }

        productId = productId.replaceFirst("^0+", "");

        Optional<Product> existingProduct = productRepository.findById(productId);

        if (existingProduct.isPresent()) {
            return existingProduct.get();
        } else {
            Product newProduct = new Product();
            newProduct.setId(productId);
            newProduct.setValue(productValue);

            return productRepository.save(newProduct);
        }
    }

    private void updateOrderWithProduct(Order order, Product product) {
        try {
            if (!order.getProducts().contains(product)) {
                order.getProducts().add(product);
            }

            BigDecimal total = calculateOrderTotal(order);
            order.setTotal(total);

            orderRepository.save(order);
        } catch (Exception e) {
            logError("Error updating order with product", e);
            throw new DatabaseOperationException("Error updating order " + order + " in database");
        }
    }

    private BigDecimal calculateOrderTotal(Order order) {
        BigDecimal total = BigDecimal.ZERO;

        for (Product product : order.getProducts()) {
            total = total.add(product.getValue());
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private String getNextProductId() {
        Integer maxId = productRepository.findMaxId();

        if (maxId == null) {
            return "1";
        }

        return String.valueOf(maxId + 1);
    }

    private boolean isInvalidProductId(String productId) {
        return productId.matches("^0+$");
    }


    private void logError(String message, Exception e) {
        log.error(message, e);
    }

    public static class FileImportException extends RuntimeException {
        public FileImportException(String message) {
            super(message);
        }
    }

    public static class DataProcessingException extends RuntimeException {
        public DataProcessingException(String message) {
            super(message);
        }
    }

    public static class DatabaseOperationException extends RuntimeException {
        public DatabaseOperationException(String message) {
            super(message);
        }
    }
}


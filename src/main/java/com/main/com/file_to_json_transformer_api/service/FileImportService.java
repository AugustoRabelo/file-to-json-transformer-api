package com.main.com.file_to_json_transformer_api.service;

import com.main.com.file_to_json_transformer_api.entity.Order;
import com.main.com.file_to_json_transformer_api.entity.Product;
import com.main.com.file_to_json_transformer_api.entity.User;
import com.main.com.file_to_json_transformer_api.repository.OrderRepository;
import com.main.com.file_to_json_transformer_api.repository.ProductRepository;
import com.main.com.file_to_json_transformer_api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class FileImportService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private static final int BATCH_SIZE = 1000;

    private static final Logger logger = LoggerFactory.getLogger(FileImportService.class);

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
                    logError("Error processing line: " + line, e);
                    invalidLines.add(line);
                } catch (Exception e) {
                    logError("Unexpected error processing line: " + line, e);
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

    private void processLine(String line, List<Order> ordersBatch) {
        try {
            String userId = line.substring(0, 10).trim();
            String userName = line.substring(10, 55).trim();
            String orderId = line.substring(55, 65).trim();
            String productId = line.substring(65, 75).trim();
            BigDecimal productValue = new BigDecimal(line.substring(75, 87).trim());
            String dateString = line.substring(87, 95).trim();
            LocalDate purchaseDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));

            if (productValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new DataProcessingException("Product value must be greater than zero");
            }

            User user = findOrCreateUser(userId, userName);
            Order order = findOrCreateOrder(orderId, purchaseDate, user);
            Product product = findOrCreateProduct(productId, productValue, order);

            updateOrderWithProduct(order, product);
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
        orderRepository.saveAll(ordersBatch);
    }

    private void logFileImportSummary(int processedLines, int invalidLinesCount) {
        System.out.println("File processing summary:");
        System.out.println("Processed lines: " + processedLines);
        System.out.println("Invalid lines: " + invalidLinesCount);
    }

    private void validateLineFormat(String line) {
        if (line == null || line.length() < 95) {
            throw new DataProcessingException("Invalid line format: line too short");
        }
    }

    private User findOrCreateUser(String userId, String userName) {
        return userRepository.findById(userId).orElseGet(() -> {
            try {
                User newUser = new User();
                newUser.setId(userId);
                newUser.setName(userName);
                userRepository.save(newUser);
                return newUser;
            } catch (Exception e) {
                logError("Error creating user: " + userId, e);
                throw new DatabaseOperationException("Error saving user to database", e);
            }
        });
    }

    private Order findOrCreateOrder(String orderId, LocalDate purchaseDate, User user) {
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
                throw new DatabaseOperationException("Error saving order to database", e);
            }
        }
        return order;
    }

    private Product findOrCreateProduct(String productId, BigDecimal productValue, Order order) {
        try {
            return productRepository.findById(productId).orElseGet(() -> {
                Product newProduct = new Product();
                newProduct.setId(productId);
                newProduct.setValue(productValue);
                newProduct.setOrder(order);
                return newProduct;
            });
        } catch (Exception e) {
            logError("Error creating product: " + productId, e);
            throw new DatabaseOperationException("Error saving product to database", e);
        }
    }

    private void updateOrderWithProduct(Order order, Product product) {
        try {
            if (order.getProducts() == null) {
                order.setProducts(new ArrayList<>());
            }
            order.getProducts().add(product);
            orderRepository.save(order);

            BigDecimal total = calculateOrderTotal(order);
            order.setTotal(total);

            orderRepository.save(order);
        } catch (Exception e) {
            logError("Error updating order with product", e);
            throw new DatabaseOperationException("Error updating order in database", e);
        }
    }

    private BigDecimal calculateOrderTotal(Order order) {
        BigDecimal total = order.getProducts().stream()
                .map(Product::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private void logError(String message, Exception e) {
        logger.error(message, e);
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
        public DatabaseOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}


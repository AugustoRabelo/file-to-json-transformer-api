package com.main.com.file_to_json_transformer_api;

import com.main.com.file_to_json_transformer_api.dto.UserDTO;
import com.main.com.file_to_json_transformer_api.entity.Order;
import com.main.com.file_to_json_transformer_api.entity.User;
import com.main.com.file_to_json_transformer_api.repository.OrderRepository;
import com.main.com.file_to_json_transformer_api.repository.UserRepository;
import com.main.com.file_to_json_transformer_api.service.DataExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class DataExportServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DataExportService dataExportService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldReturnOrderByIdWithSuccess() {
        String orderId = "12345";
        User mockUser = new User();
        mockUser.setId("user123");
        mockUser.setName("Chico Bento");

        Order mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setDate(LocalDate.of(2025, 3, 31));
        mockOrder.setTotal(new BigDecimal("100.00"));
        mockOrder.setUser(mockUser);

        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(mockOrder));

        UserDTO result = dataExportService.getOrderById(orderId);

        assertNotNull(result);
        assertEquals("user123", result.getUserId());
        assertEquals("Chico Bento", result.getName());
        assertEquals(orderId, result.getOrder().getOrderId());
        assertEquals(new BigDecimal("100.00"), result.getOrder().getTotal());

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    public void shouldReturnOrdersByDateRangeWithSuccess() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);

        User mockUser = new User();
        mockUser.setId("user123");
        mockUser.setName("Chico Bento");

        Order mockOrder = new Order();
        mockOrder.setId("12345");
        mockOrder.setDate(LocalDate.of(2025, 3, 31));
        mockOrder.setTotal(new BigDecimal("100.00"));
        mockOrder.setUser(mockUser);

        when(orderRepository.findByDateBetween(startDate, endDate, Sort.by(Sort.Order.asc("date"))))
                .thenReturn(java.util.List.of(mockOrder));

        List<UserDTO> result = dataExportService.getOrdersByDateRange(startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("12345", result.getFirst().getOrder().getOrderId());
        assertEquals(new BigDecimal("100.00"), result.getFirst().getOrder().getTotal());
    }

    @Test
    public void shouldThrowExceptionWhenOrderNotFound() {
        String orderId = "99999";

        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> dataExportService.getOrderById(orderId));
    }

    @Test
    public void shouldReturnEmptyListWhenNoOrdersInDateRange() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);

        when(orderRepository.findByDateBetween(startDate, endDate, Sort.by(Sort.Order.asc("date"))))
                .thenReturn(java.util.List.of());

        List<UserDTO> result = dataExportService.getOrdersByDateRange(startDate, endDate);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldThrowExceptionWhenOrderIdIsEmpty() {
        assertThrows(RuntimeException.class, () -> dataExportService.getOrderById(""));
    }

    @Test
    public void shouldThrowExceptionWhenOrderIdIsNull() {
        assertThrows(RuntimeException.class, () -> dataExportService.getOrderById(null));
    }

    @Test
    public void shouldReturnOrdersInAscendingOrderByDate() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);

        User mockUser1 = new User();
        mockUser1.setId("user123");
        mockUser1.setName("Chico Bento");

        User mockUser2 = new User();
        mockUser2.setId("user456");
        mockUser2.setName("Maria Joaquina");

        Order mockOrder1 = new Order();
        mockOrder1.setId("12345");
        mockOrder1.setDate(LocalDate.of(2025, 3, 31));  // Data mais antiga
        mockOrder1.setTotal(new BigDecimal("100.00"));
        mockOrder1.setUser(mockUser1);

        Order mockOrder2 = new Order();
        mockOrder2.setId("67890");
        mockOrder2.setDate(LocalDate.of(2025, 6, 15));  // Data mais recente
        mockOrder2.setTotal(new BigDecimal("150.00"));
        mockOrder2.setUser(mockUser2);

        List<Order> orders = new ArrayList<>();
        orders.add(mockOrder2);
        orders.add(mockOrder1);

        when(orderRepository.findByDateBetween(startDate, endDate, Sort.by(Sort.Order.asc("date"))))
                .thenReturn(orders.stream().sorted(Comparator.comparing(Order::getDate)).collect(Collectors.toList()));

        List<UserDTO> result = dataExportService.getOrdersByDateRange(startDate, endDate);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("12345", result.get(0).getOrder().getOrderId());
        assertEquals("67890", result.get(1).getOrder().getOrderId());

        assertNotNull(result.get(0).getUserId());
        assertNotNull(result.get(1).getUserId());
        assertEquals("user123", result.get(0).getUserId());
        assertEquals("user456", result.get(1).getUserId());
    }
}

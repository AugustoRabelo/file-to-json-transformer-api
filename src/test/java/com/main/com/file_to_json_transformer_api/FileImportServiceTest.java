package com.main.com.file_to_json_transformer_api;

import com.main.com.file_to_json_transformer_api.entity.Order;
import com.main.com.file_to_json_transformer_api.entity.Product;
import com.main.com.file_to_json_transformer_api.entity.User;
import com.main.com.file_to_json_transformer_api.repository.OrderRepository;
import com.main.com.file_to_json_transformer_api.repository.ProductRepository;
import com.main.com.file_to_json_transformer_api.repository.UserRepository;
import com.main.com.file_to_json_transformer_api.service.FileImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileImportServiceTest {

    @InjectMocks
    private FileImportService fileImportService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Test
    void shouldImportFileSuccessfully() throws Exception {
        String validLine = "0000000070                              Palmer Prosacco00000007530000000003     1836.7420210308";

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(validLine.getBytes()));

        User mockUser = new User();
        mockUser.setId("70");
        mockUser.setName("Palmer Prosacco");

        Product mockProduct = new Product();
        mockProduct.setId("3");
        mockProduct.setValue(new BigDecimal("1836.74"));

        Order mockOrder = new Order();
        mockOrder.setId("753");
        mockOrder.setDate(LocalDate.now());
        mockOrder.setUser(mockUser);
        mockOrder.setProducts(new ArrayList<>());

        when(userRepository.findById("70")).thenReturn(Optional.of(mockUser));

        when(productRepository.findById("3")).thenReturn(Optional.of(mockProduct));

        when(orderRepository.saveAll(anyList())).thenReturn(List.of(mockOrder));

        ArgumentCaptor<List<Order>> orderCaptor = ArgumentCaptor.forClass(List.class);

        fileImportService.importFile(mockFile);

        verify(orderRepository, times(1)).saveAll(orderCaptor.capture());

        List<Order> savedOrders = orderCaptor.getValue();

        assertNotNull(savedOrders);
        assertEquals(1, savedOrders.size());

        Order savedOrder = savedOrders.get(0);
        assertEquals("753", savedOrder.getId());
        assertEquals("70", savedOrder.getUser().getId());
        assertEquals(1, savedOrder.getProducts().size());
        assertTrue(savedOrder.getProducts().contains(mockProduct));
        assertEquals(new BigDecimal("1836.74"), savedOrder.getTotal());

        verify(productRepository, times(1)).findById("3");
        verify(userRepository, times(1)).findById("70");
    }

    @Test
    void shouldThrowExceptionWhenFileIsEmpty() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        assertThrows(FileImportService.FileImportException.class, () -> {
            fileImportService.importFile(mockFile);
        });
    }

}

package com.main.com.file_to_json_transformer_api.controller;

import com.main.com.file_to_json_transformer_api.dto.UserDTO;
import com.main.com.file_to_json_transformer_api.service.DataExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DataExportController {

    @Autowired
    private DataExportService dataExportService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<UserDTO> getOrderById(@PathVariable String orderId) {
        try {
            UserDTO userDTO = dataExportService.getOrderById(orderId);
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/order/byDateRange")
    public ResponseEntity<List<UserDTO>> getOrdersByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String endDate) {

        try {
            System.out.println("Start Date: " + startDate);
            System.out.println("End Date: " + endDate);

            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            List<UserDTO> userDTOs = dataExportService.getOrdersByDateRange(start, end);
            return ResponseEntity.ok(userDTOs);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

}

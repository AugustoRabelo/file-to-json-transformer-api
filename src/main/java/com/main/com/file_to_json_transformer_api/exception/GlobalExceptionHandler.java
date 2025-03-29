package com.main.com.file_to_json_transformer_api.exception;

import com.main.com.file_to_json_transformer_api.service.FileImportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileImportService.FileImportException.class)
    public ResponseEntity<String> handleFileImportException(FileImportService.FileImportException e) {
        return new ResponseEntity<>("Error processing file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileImportService.DataProcessingException.class)
    public ResponseEntity<String> handleDataProcessingException(FileImportService.DataProcessingException e) {
        return new ResponseEntity<>("Error processing data: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FileImportService.DatabaseOperationException.class)
    public ResponseEntity<String> handleDatabaseOperationException(FileImportService.DatabaseOperationException e) {
        return new ResponseEntity<>("Error saving data to database: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e) {
        return new ResponseEntity<>("An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

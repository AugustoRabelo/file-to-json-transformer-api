package com.main.com.file_to_json_transformer_api.controller;

import com.main.com.file_to_json_transformer_api.service.FileImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/import")
public class FileImportController {

    @Autowired
    private FileImportService fileImportService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        fileImportService.importFile(file);
        return ResponseEntity.ok("File processed successfully");
    }
}

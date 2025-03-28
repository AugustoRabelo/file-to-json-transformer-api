package com.main.com.file_to_json_transformer_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class FileToJsonApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileToJsonApplication.class, args);
	}

}

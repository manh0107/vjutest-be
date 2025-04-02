package com.example.vjutest;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.example.vjutest")
public class VjutestApplication {

	 public static void main(String[] args) {
        SpringApplication app = new SpringApplication(VjutestApplication.class);
        app.setDefaultProperties(Collections
          .singletonMap("server.port", "8080"));
        app.run(args);
    }

}

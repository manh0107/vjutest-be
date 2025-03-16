package com.example.vjutest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;


@SpringBootApplication
@EntityScan(basePackages = "com.example.vjutest")
public class VjutestApplication {

	public static void main(String[] args) {
		SpringApplication.run(VjutestApplication.class, args);
	}

}

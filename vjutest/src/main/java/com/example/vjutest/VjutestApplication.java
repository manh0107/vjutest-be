package com.example.vjutest;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EntityScan(basePackages = "com.example.vjutest")
@EnableJpaRepositories(basePackages = "com.example.vjutest")
@EnableTransactionManagement
public class VjutestApplication {

	 public static void main(String[] args) {
        SpringApplication app = new SpringApplication(VjutestApplication.class);
        app.setDefaultProperties(Collections
          .singletonMap("server.port", "8080"));
        app.run(args);
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new JpaTransactionManager();
    }

}

package com.example.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.customer")
public class CustomerManagementServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerManagementServiceApplication.class, args);
    }
}


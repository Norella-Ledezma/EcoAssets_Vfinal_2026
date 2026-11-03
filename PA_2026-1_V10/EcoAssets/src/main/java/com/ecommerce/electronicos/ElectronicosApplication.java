package com.ecommerce.electronicos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ElectronicosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElectronicosApplication.class, args);
    }
}

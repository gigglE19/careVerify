package com.careverify.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.careverify")
public class CareVerifyApplication {
    public static void main(String[] args) {
        SpringApplication.run(CareVerifyApplication.class, args);
    }
}


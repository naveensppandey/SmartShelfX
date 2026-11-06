package com.smartshelfx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SmartShelfXApplication
 * 
 * The main entry point for the SmartShelfX Spring Boot Backend application.
 * 
 * Flow:
 * Request -> Controller -> Service -> Repository -> MySQL Database -> Response
 */
@SpringBootApplication
public class SmartShelfXApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartShelfXApplication.class, args);
    }
}

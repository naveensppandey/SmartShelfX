package com.smartshelfx.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplateConfig
 * 
 * Provides a managed RestTemplate bean with connection and read timeouts.
 * Used by the backend to call the Python AI demand forecasting microservice.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofMillis(5000))
                .readTimeout(Duration.ofMillis(5000))
                .build();
    }
}

package com.autocare.workshop_service.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced // Magia pura: permite llamar a "http://inventory-service" sin saber su puerto real
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
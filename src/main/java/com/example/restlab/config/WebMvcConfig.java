package com.example.restlab.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC & Cross-Origin Resource Sharing (CORS) Configuration (Experiment 2.1.1)
 * Enables secure cross-origin communication between modern frontend clients and backend APIs.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.security.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:8080}")
    private List<String> allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-Correlation-ID", "Authorization")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

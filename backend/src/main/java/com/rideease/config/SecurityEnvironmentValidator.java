package com.rideease.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Validates that essential security configuration and credentials
 * are present in the environment before allowing application startup.
 */
@Configuration
public class SecurityEnvironmentValidator {

    @Value("${spring.data.mongodb.uri:}")
    private String mongoUri;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @PostConstruct
    public void validateRequiredSecrets() {
        if (mongoUri == null || mongoUri.isBlank()) {
            throw new IllegalStateException(
                    "FATAL STARTUP FAILURE: 'SPRING_DATA_MONGODB_URI' is not configured. " +
                    "Set the SPRING_DATA_MONGODB_URI environment variable before starting RideEase."
            );
        }

        if (jwtSecret == null || jwtSecret.isBlank() || jwtSecret.length() < 32) {
            throw new IllegalStateException(
                    "FATAL STARTUP FAILURE: 'JWT_SECRET' is missing or too short. " +
                    "Set a secure JWT_SECRET environment variable of at least 32 characters."
            );
        }
    }
}

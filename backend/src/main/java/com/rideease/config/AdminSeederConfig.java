package com.rideease.config;

import com.rideease.model.Role;
import com.rideease.model.User;
import com.rideease.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

/**
 * Initializes an administrator account only when explicit configuration
 * is provided via environment variables. Does NOT seed default credentials.
 */
@Configuration
public class AdminSeederConfig {

    private static final Logger log = LoggerFactory.getLogger(AdminSeederConfig.class);

    @Value("${admin.initial.email:}")
    private String adminInitialEmail;

    @Value("${admin.initial.password:}")
    private String adminInitialPassword;

    @Bean
    public CommandLineRunner seedInitialAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (adminInitialEmail == null || adminInitialEmail.isBlank() ||
                adminInitialPassword == null || adminInitialPassword.isBlank()) {
                log.info("No initial administrator environment variables provided. Skipping admin seeding.");
                return;
            }

            String trimmedEmail = adminInitialEmail.trim();
            if (!userRepository.existsByEmail(trimmedEmail)) {
                User admin = new User(
                        null,
                        "System Administrator",
                        trimmedEmail,
                        "+910000000000",
                        passwordEncoder.encode(adminInitialPassword),
                        Role.ADMIN,
                        null,
                        true,
                        Instant.now(),
                        Instant.now()
                );
                userRepository.save(admin);
                log.info("Initial administrator account provisioned for configured email: {}", trimmedEmail);
            }
        };
    }
}

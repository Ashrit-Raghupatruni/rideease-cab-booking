package com.rideease.config;

import com.rideease.model.User;
import com.rideease.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@Configuration
public class AdminSeederConfig {

    @Bean
    public CommandLineRunner seedDefaultAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@rideease.com";
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = new User(
                        null,
                        "Super Administrator",
                        adminEmail,
                        "+919999999999",
                        passwordEncoder.encode("admin123"),
                        com.rideease.model.Role.ADMIN,
                        null,
                        true,
                        Instant.now(),
                        Instant.now()
                );
                userRepository.save(admin);
                System.out.println(">>> Default Admin created: admin@rideease.com / admin123");
            }
        };
    }
}

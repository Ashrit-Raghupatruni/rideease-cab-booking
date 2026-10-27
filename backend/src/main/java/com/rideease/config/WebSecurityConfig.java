package com.rideease.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable for REST / WebSocket testing
            .authorizeHttpRequests(auth -> auth
                // Allow all frontend views, static assets, and testing endpoints
                .requestMatchers(
                    "/",
                    "/user/**",
                    "/driver/**",
                    "/admin/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/api/auth/**",
                    "/api/rides/**",
                    "/api/drivers/**",
                    "/api/geo/**",
                    "/api/health/**",
                    "/ws-ride/**"
                ).permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }
}

package com.rideease.javafx.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Enterprise Asynchronous API Client for JavaFX Desktop App
 * Communicates directly with RideEase Spring Boot Backend (Ports 8080/8081/8082)
 * Fully non-blocking to prevent any freeze on the JavaFX Application Thread.
 */
public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(4))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public <T> CompletableFuture<T> getAsync(String endpoint, TypeReference<T> typeRef) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(6))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        try {
                            return objectMapper.readValue(response.body(), typeRef);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to parse JSON response: " + e.getMessage(), e);
                        }
                    } else {
                        throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
                    }
                });
    }

    public CompletableFuture<Map<String, Object>> postFormAsync(String endpoint, Map<String, String> formData) {
        StringBuilder formBody = new StringBuilder();
        if (formData != null) {
            for (Map.Entry<String, String> entry : formData.entrySet()) {
                if (formBody.length() > 0) formBody.append("&");
                formBody.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue() != null ? entry.getValue() : "", StandardCharsets.UTF_8));
            }
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(8))
                .POST(HttpRequest.BodyPublishers.ofString(formBody.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
                    } catch (Exception e) {
                        throw new RuntimeException("HTTP " + response.statusCode() + " parse error: " + response.body(), e);
                    }
                });
    }
}

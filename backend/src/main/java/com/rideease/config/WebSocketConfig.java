package com.rideease.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Topic prefixes for subscriptions (ride updates, driver tracking)
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. Raw WebSocket endpoint for JavaFX native client & Android
        registry.addEndpoint("/ws-ride")
                .setAllowedOriginPatterns("*");

        // 2. SockJS fallback endpoint for Web Browsers (Thymeleaf / React)
        registry.addEndpoint("/ws-ride")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}

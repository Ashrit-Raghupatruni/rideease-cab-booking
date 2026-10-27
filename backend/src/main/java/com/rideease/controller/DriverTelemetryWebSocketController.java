package com.rideease.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles real-time driver GPS telemetry broadcasts.
 * Drivers send updates to /app/driver/telemetry, and subscribers to
 * /topic/ride/{bookingId}/telemetry receive live coordinates.
 */
@Controller
public class DriverTelemetryWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(DriverTelemetryWebSocketController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/driver/telemetry")
    public void handleDriverTelemetry(@Payload Map<String, Object> payload) {
        try {
            String bookingId = (String) payload.get("bookingId");
            Number lat = (Number) payload.get("lat");
            Number lon = (Number) payload.get("lon");

            if (bookingId != null && lat != null && lon != null) {
                Map<String, Object> update = new HashMap<>();
                update.put("bookingId", bookingId);
                update.put("lat", lat.doubleValue());
                update.put("lon", lon.doubleValue());
                update.put("timestamp", Instant.now().toString());

                // Broadcast live location to passenger tracking screen
                messagingTemplate.convertAndSend("/topic/ride/" + bookingId + "/telemetry", update);
                log.debug("Broadcast driver telemetry for booking {}: ({}, {})", bookingId, lat, lon);
            }
        } catch (Exception e) {
            log.error("Error processing driver telemetry", e);
        }
    }
}

package com.rideease.service;

import com.rideease.model.Booking;
import com.rideease.model.Booking.BookingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enterprise Real-Time Dispatch & Telemetry Service
 * - Manages last-known GPS coordinates per booking
 * - Broadcasts authenticated booking lifecycle events (SEARCHING, ACCEPTED, ARRIVED, IN_PROGRESS, COMPLETED, CANCELLED)
 * - Broadcasts live incoming ride requests to driver partner terminals
 * - Pushes fleet KPI changes to admin console
 */
@Service
public class RealtimeDispatchService {

    private static final Logger log = LoggerFactory.getLogger(RealtimeDispatchService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Cache last-known location snapshots for instant delivery upon passenger / admin reconnection
    private final Map<String, Map<String, Object>> lastKnownTelemetry = new ConcurrentHashMap<>();

    /**
     * Broadcasts a status transition event to passenger and admin
     */
    public void broadcastBookingStatus(Booking booking, String extraMessage) {
        if (booking == null) return;

        Map<String, Object> event = new HashMap<>();
        event.put("bookingId", booking.getId());
        event.put("status", booking.getStatus().name());
        event.put("otp", booking.getOtp());
        event.put("fare", booking.getEstimatedFare());
        event.put("driverId", booking.getDriverId());
        event.put("message", extraMessage != null ? extraMessage : "Status updated to " + booking.getStatus());
        event.put("timestamp", Instant.now().toString());

        // 1. Send to booking-specific passenger topic
        messagingTemplate.convertAndSend("/topic/ride/" + booking.getId() + "/status", event);

        // 2. Send to admin live operations fleet event topic
        Map<String, Object> adminEvent = new HashMap<>(event);
        adminEvent.put("pickup", booking.getPickupAddress());
        adminEvent.put("dropoff", booking.getDropoffAddress());
        adminEvent.put("category", booking.getVehicleCategory() != null ? booking.getVehicleCategory().name() : "SEDAN");
        messagingTemplate.convertAndSend("/topic/admin/fleet/events", adminEvent);

        log.info("Broadcasted booking {} transition to {}", booking.getId(), booking.getStatus());
    }

    /**
     * Broadcasts a new incoming booking request to all active driver terminals
     */
    public void broadcastNewRideRequest(Booking booking) {
        if (booking == null) return;

        Map<String, Object> requestEvent = new HashMap<>();
        requestEvent.put("bookingId", booking.getId());
        requestEvent.put("pickupAddress", booking.getPickupAddress());
        requestEvent.put("dropoffAddress", booking.getDropoffAddress());
        requestEvent.put("fare", booking.getEstimatedFare());
        requestEvent.put("distanceKm", booking.getDistanceKm());
        requestEvent.put("durationMinutes", booking.getEstimatedDurationMinutes());
        requestEvent.put("vehicleCategory", booking.getVehicleCategory() != null ? booking.getVehicleCategory().name() : "SEDAN");
        requestEvent.put("timestamp", Instant.now().toString());

        messagingTemplate.convertAndSend("/topic/drivers/requests", requestEvent);
        log.info("Broadcasted new ride request for booking {} to /topic/drivers/requests", booking.getId());
    }

    /**
     * Stores and forwards driver GPS telemetry
     */
    public void forwardTelemetry(String bookingId, Map<String, Object> telemetryData) {
        lastKnownTelemetry.put(bookingId, telemetryData);
        messagingTemplate.convertAndSend("/topic/ride/" + bookingId + "/telemetry", telemetryData);
        messagingTemplate.convertAndSend("/topic/admin/fleet/telemetry", telemetryData);
    }

    /**
     * Retrieves last known snapshot for reconnecting clients
     */
    public Map<String, Object> getLastKnownTelemetry(String bookingId) {
        return lastKnownTelemetry.get(bookingId);
    }

    /**
     * Clears telemetry cache once trip completes
     */
    public void purgeTelemetry(String bookingId) {
        lastKnownTelemetry.remove(bookingId);
    }
    public void broadcastCancelToDrivers(String bookingId, String message) {
        Map<String, Object> cancelEvent = new HashMap<>();
        cancelEvent.put("bookingId", bookingId);
        cancelEvent.put("status", "CANCELLED");
        cancelEvent.put("action", "CANCEL_REQUEST");
        cancelEvent.put("message", message != null ? message : "Ride cancelled");
        messagingTemplate.convertAndSend("/topic/drivers/requests", cancelEvent);
        log.info("Broadcasted cancellation for booking {} to /topic/drivers/requests", bookingId);
    }

}

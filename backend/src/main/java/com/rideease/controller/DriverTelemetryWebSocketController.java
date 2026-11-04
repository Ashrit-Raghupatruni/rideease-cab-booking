package com.rideease.controller;

import com.rideease.model.Booking;
import com.rideease.model.Booking.BookingStatus;
import com.rideease.repository.BookingRepository;
import com.rideease.service.RealtimeDispatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Enterprise Driver GPS Telemetry & Ride Event Controller
 * - Validates coordinate sanity (-90..90, -180..180)
 * - Verifies booking existence, driver assignment, and active status
 * - Computes arrival proximity alert (< 75m)
 * - Provides last-known snapshot upon subscription reconnection
 */
@Controller
public class DriverTelemetryWebSocketController {

    private static final Logger log = LoggerFactory.getLogger(DriverTelemetryWebSocketController.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RealtimeDispatchService realtimeDispatchService;

    @MessageMapping("/driver/telemetry")
    public void handleDriverTelemetry(@Payload Map<String, Object> payload) {
        try {
            String bookingId = (String) payload.get("bookingId");
            String driverId = (String) payload.get("driverId");
            Number latNum = (Number) payload.get("lat");
            Number lonNum = (Number) payload.get("lon");
            Number headingNum = (Number) payload.get("heading");
            Number speedNum = (Number) payload.get("speed");

            if (bookingId == null || latNum == null || lonNum == null) {
                log.warn("Rejected telemetry: missing mandatory coordinates or booking ID");
                return;
            }

            double lat = latNum.doubleValue();
            double lon = lonNum.doubleValue();

            // Coordinate Sanity Check
            if (lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
                log.warn("Rejected telemetry: coordinate out of bounds ({}, {})", lat, lon);
                return;
            }

            // Verify Booking & Driver Assignment
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isEmpty()) {
                log.warn("Rejected telemetry: booking {} not found", bookingId);
                return;
            }

            Booking booking = bookingOpt.get();

            // Telemetry is ONLY allowed for active in-transit statuses
            if (booking.getStatus() != BookingStatus.ACCEPTED && 
                booking.getStatus() != BookingStatus.ARRIVING && 
                booking.getStatus() != BookingStatus.IN_PROGRESS) {
                log.warn("Rejected telemetry: booking {} is inactive ({})", bookingId, booking.getStatus());
                return;
            }

            // Verify Driver Assignment if provided in payload
            if (driverId != null && booking.getDriverId() != null && !driverId.equals(booking.getDriverId())) {
                log.warn("Rejected telemetry: driver {} is not assigned to booking {}", driverId, bookingId);
                return;
            }

            // Compute distance to pickup to trigger Arrival alert if driver is within 75 meters
            boolean isNearPickup = false;
            if (booking.getStatus() == BookingStatus.ACCEPTED && booking.getPickupLatitude() != null && booking.getPickupLongitude() != null) {
                double pLon = booking.getPickupLongitude();
                double pLat = booking.getPickupLatitude();
                double distanceMeters = calculateHaversineMeters(lat, lon, pLat, pLon);

                if (distanceMeters <= 75.0) {
                    isNearPickup = true;
                    // Trigger ARRIVED status transition if not already triggered
                    booking.setStatus(BookingStatus.ARRIVING);
                    bookingRepository.save(booking);
                    realtimeDispatchService.broadcastBookingStatus(booking, "Captain has arrived at your pickup spot!");
                }
            }

            Map<String, Object> update = new HashMap<>();
            update.put("bookingId", bookingId);
            update.put("lat", lat);
            update.put("lon", lon);
            update.put("heading", headingNum != null ? headingNum.doubleValue() : 0.0);
            update.put("speed", speedNum != null ? speedNum.doubleValue() : 0.0);
            update.put("isNearPickup", isNearPickup);
            update.put("status", booking.getStatus().name());
            update.put("timestamp", Instant.now().toString());

            // Forward to passenger and admin telemetry streams
            realtimeDispatchService.forwardTelemetry(bookingId, update);

        } catch (Exception e) {
            log.error("Error processing driver telemetry", e);
        }
    }

    /**
     * Reconnection snapshot support:
     * When a client subscribes to /app/ride/{bookingId}/snapshot, returns the last-known telemetry state.
     */
    @SubscribeMapping("/ride/{bookingId}/snapshot")
    public Map<String, Object> getSnapshot(@Payload(required = false) Map<String, Object> ignored, 
                                          @org.springframework.messaging.handler.annotation.DestinationVariable String bookingId) {
        Map<String, Object> snapshot = realtimeDispatchService.getLastKnownTelemetry(bookingId);
        if (snapshot == null) {
            snapshot = new HashMap<>();
            snapshot.put("bookingId", bookingId);
            snapshot.put("available", false);
        }
        return snapshot;
    }

    private double calculateHaversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}

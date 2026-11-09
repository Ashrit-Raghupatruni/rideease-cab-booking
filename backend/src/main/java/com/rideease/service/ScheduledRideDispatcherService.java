package com.rideease.service;

import com.rideease.model.Booking;
import com.rideease.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Periodically queries scheduled rides and automatically transitions them
 * to SEARCHING and dispatches to nearby drivers when within the dispatch window.
 */
@Service
public class ScheduledRideDispatcherService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledRideDispatcherService.class);

    // Lookahead window: dispatch rides 20 minutes before pickup time
    private static final Duration DISPATCH_WINDOW = Duration.ofMinutes(20);

    private final BookingRepository bookingRepository;
    private final RealtimeDispatchService realtimeDispatchService;

    public ScheduledRideDispatcherService(BookingRepository bookingRepository, RealtimeDispatchService realtimeDispatchService) {
        this.bookingRepository = bookingRepository;
        this.realtimeDispatchService = realtimeDispatchService;
    }

    /**
     * Runs every 60 seconds to scan for scheduled rides that are due for dispatch.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void dispatchUpcomingScheduledRides() {
        Instant threshold = Instant.now().plus(DISPATCH_WINDOW);

        List<Booking> dueBookings = bookingRepository.findByScheduledTrueAndStatusAndScheduledTimeLessThanEqual(
                Booking.BookingStatus.SCHEDULED, threshold
        );

        if (dueBookings.isEmpty()) {
            return;
        }

        log.info("Found {} scheduled ride(s) due for dispatch (threshold: {})", dueBookings.size(), threshold);

        for (Booking booking : dueBookings) {
            try {
                booking.setStatus(Booking.BookingStatus.SEARCHING);
                booking.setUpdatedAt(Instant.now());
                Booking updated = bookingRepository.save(booking);

                // Broadcast to drivers and passenger
                realtimeDispatchService.broadcastNewRideRequest(updated);
                realtimeDispatchService.broadcastBookingStatus(updated, "Scheduled pickup time approaching. Matching with nearby drivers.");
                log.info("Successfully dispatched scheduled booking {}", updated.getId());
            } catch (Exception e) {
                log.error("Error dispatching scheduled booking {}: {}", booking.getId(), e.getMessage(), e);
            }
        }
    }
}

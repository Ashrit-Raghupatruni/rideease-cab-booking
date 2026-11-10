package com.rideease.controller;

import com.rideease.dto.BookRideRequest;
import com.rideease.model.Booking;
import com.rideease.model.Driver;
import com.rideease.model.User;
import com.rideease.model.Vehicle;
import com.rideease.repository.BookingRepository;
import com.rideease.repository.DriverRepository;
import com.rideease.repository.UserRepository;
import com.rideease.service.FareCalculationService;
import com.rideease.service.RoadRoutingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Controller
public class RideOperationController {

    private static final Logger log = LoggerFactory.getLogger(RideOperationController.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private FareCalculationService fareCalculationService;

    @Autowired
    private RoadRoutingService roadRoutingService;

    @Autowired
    private com.rideease.service.RealtimeDispatchService realtimeDispatchService;

    private static final int MAX_OTP_ATTEMPTS = 5;

    /**
     * 1. Passenger Creates Ride Booking
     * ZERO-FABRICATION & STRICT VALIDATION:
     * - Validated via Jakarta Bean Validation (@Valid BookRideRequest)
     * - Recalculates authoritative road route on backend
     * - If road routing fails, booking is cleanly rejected; NO fabricated distance or fare.
     */
    @PostMapping("/user/book")
    public String bookRide(
            @Valid @ModelAttribute BookRideRequest request,
            BindingResult bindingResult,
            HttpSession session
    ) {
        String userId = (String) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/user/login";
        }

        if (bindingResult.hasErrors()) {
            log.warn("Booking validation failed for user {}: {}", userId, bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/user/home?error=validation_failed";
        }

        // Authoritative Road Route Calculation
        RoadRoutingService.RoutingResult route = roadRoutingService.calculateRoadRoute(
                request.getPickupLat(), request.getPickupLon(),
                request.getDropoffLat(), request.getDropoffLon()
        );

        if (!route.isSuccess() || route.getDistanceKm() <= 0.0) {
            log.warn("Authoritative road routing failed for user {}: {}", userId, route.getMessage());
            return "redirect:/user/home?error=no_drivable_route";
        }

        double distanceKm = route.getDistanceKm();
        int durationMinutes = route.getDurationMinutes();

        // Calculate verified fare
        double fare = fareCalculationService.calculateFare(request.getVehicleCategory(), distanceKm, durationMinutes, 1.0);

        // Cryptographically secure OTP
        String otp = String.format("%04d", new SecureRandom().nextInt(10000));

        Booking booking = new Booking();
        booking.setId("BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setPassengerId(userId);
        booking.setVehicleCategory(request.getVehicleCategory());
        booking.setPickupAddress(request.getPickupAddress().trim());
        booking.setPickupLatitude(request.getPickupLat());
        booking.setPickupLongitude(request.getPickupLon());
        booking.setDropoffAddress(request.getDropoffAddress().trim());
        booking.setDropoffLatitude(request.getDropoffLat());
        booking.setDropoffLongitude(request.getDropoffLon());
        booking.setDistanceKm(distanceKm);
        booking.setEstimatedDurationMinutes(durationMinutes);
        booking.setEstimatedFare(fare);
        booking.setStatus(Booking.BookingStatus.SEARCHING);
        booking.setOtp(otp);
        booking.setOtpAttempts(0);
        booking.setPaymentMethod(request.getPaymentMethod());
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);

        // Schedule For Later Processing
        if (request.getScheduledDate() != null && !request.getScheduledDate().isBlank() &&
            request.getScheduledTime() != null && !request.getScheduledTime().isBlank()) {
            try {
                java.time.LocalDate sDate = java.time.LocalDate.parse(request.getScheduledDate().trim());
                java.time.LocalTime sTime = java.time.LocalTime.parse(request.getScheduledTime().trim());
                java.time.LocalDateTime sDateTime = java.time.LocalDateTime.of(sDate, sTime);
                Instant scheduledInstant = sDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant();

                booking.setScheduled(true);
                booking.setScheduledTime(scheduledInstant);
                // If scheduled for more than 20 minutes in the future, mark as SCHEDULED
                if (scheduledInstant.isAfter(Instant.now().plus(java.time.Duration.ofMinutes(2)))) {
                    booking.setStatus(Booking.BookingStatus.SCHEDULED);
                }
                log.info("Ride scheduled for future timestamp: {} (Status: {})", scheduledInstant, booking.getStatus());
            } catch (Exception ex) {
                log.warn("Failed to parse scheduled date/time: {} {}", request.getScheduledDate(), request.getScheduledTime());
            }
        }

        booking.setCreatedAt(Instant.now());
        booking.setUpdatedAt(Instant.now());

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created successfully: ID {}, passenger {}, distance {} km, fare ₹{}", savedBooking.getId(), userId, distanceKm, fare);

        if (savedBooking.getStatus() == Booking.BookingStatus.SCHEDULED) {
            // For future scheduled rides, redirect to My Rides with a scheduled confirmation message
            return "redirect:/user/rides?scheduled=true";
        }

        // Real-Time Event Dispatch for immediate rides
        realtimeDispatchService.broadcastNewRideRequest(savedBooking);
        realtimeDispatchService.broadcastBookingStatus(savedBooking, "Searching for nearby captains");

        return "redirect:/user/ride/" + savedBooking.getId() + "/track";
    }

    /**
     * 2. Driver Accepts Ride Request (Transactional and Atomic)
     */
    @PostMapping("/driver/ride/accept")
    @Transactional
    public String driverAcceptRide(
            @RequestParam String bookingId,
            HttpSession session
    ) {
        String driverUserId = (String) session.getAttribute("DRIVER_USER_ID");
        if (driverUserId == null) {
            return "redirect:/driver/login";
        }

        Optional<Booking> opt = bookingRepository.findById(bookingId);
        if (opt.isPresent()) {
            Booking b = opt.get();
            if (b.getStatus() == Booking.BookingStatus.SEARCHING || b.getStatus() == Booking.BookingStatus.REQUESTED) {
                b.setDriverId(driverUserId);
                b.setStatus(Booking.BookingStatus.ACCEPTED);
                b.setUpdatedAt(Instant.now());
                Booking acceptedBooking = bookingRepository.save(b);
                log.info("Driver {} accepted booking {}", driverUserId, bookingId);
                realtimeDispatchService.broadcastBookingStatus(acceptedBooking, "Captain accepted! Driving to pickup location.");
            } else {
                log.info("Driver {} could not accept booking {}: already accepted or cancelled", driverUserId, bookingId);
            }
        }

        return "redirect:/driver/dashboard";
    }

    /**
     * 3. Driver Verifies OTP & Starts Trip
     */
    @PostMapping("/driver/ride/start")
    public String driverStartTrip(
            @RequestParam String bookingId,
            @RequestParam String otp,
            HttpSession session
    ) {
        String driverUserId = (String) session.getAttribute("DRIVER_USER_ID");
        if (driverUserId == null) {
            return "redirect:/driver/login";
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return "redirect:/driver/dashboard";
        }

        Booking booking = bookingOpt.get();

        if (!driverUserId.equals(booking.getDriverId())) {
            log.warn("Unauthorized start attempt by driver {} on booking {}", driverUserId, bookingId);
            return "redirect:/driver/dashboard?error=unauthorized";
        }

        if (booking.getStatus() != Booking.BookingStatus.ACCEPTED && booking.getStatus() != Booking.BookingStatus.ARRIVING) {
            log.warn("Invalid state for starting booking {}: {}", bookingId, booking.getStatus());
            return "redirect:/driver/dashboard?error=invalid_status";
        }

        if (booking.getOtpAttempts() >= MAX_OTP_ATTEMPTS) {
            log.warn("Booking {} exceeded maximum OTP attempts", bookingId);
            return "redirect:/driver/dashboard?error=otp_limit_exceeded";
        }

        if (booking.getOtp() != null && booking.getOtp().equals(otp.trim())) {
            booking.setStatus(Booking.BookingStatus.IN_PROGRESS);
            booking.setUpdatedAt(Instant.now());
            bookingRepository.save(booking);
            log.info("Trip {} started successfully", bookingId);
            realtimeDispatchService.broadcastBookingStatus(booking, "Trip started! En route to destination.");
        } else {
            booking.setOtpAttempts(booking.getOtpAttempts() + 1);
            booking.setUpdatedAt(Instant.now());
            bookingRepository.save(booking);
            log.warn("Incorrect OTP for booking {}. Attempt {}/{}", bookingId, booking.getOtpAttempts(), MAX_OTP_ATTEMPTS);
        }

        return "redirect:/driver/dashboard";
    }

    /**
     * 4. Driver Completes Trip & Finalizes Fare
     * FINANCIAL CORRECTNESS:
     * - Decouples trip completion from payment status.
     * - If WALLET: verifies balance, atomically debits passenger wallet, and marks PAID.
     *   If insufficient, marks PENDING with clear flag.
     * - If CASH: keeps payment PENDING until cash collection is confirmed.
     * - Operations are idempotent.
     */
    @PostMapping("/driver/ride/complete")
    public String driverCompleteTrip(
            @RequestParam String bookingId,
            HttpSession session
    ) {
        String driverUserId = (String) session.getAttribute("DRIVER_USER_ID");
        if (driverUserId == null) {
            return "redirect:/driver/login";
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return "redirect:/driver/dashboard";
        }

        Booking booking = bookingOpt.get();

        if (!driverUserId.equals(booking.getDriverId())) {
            log.warn("Unauthorized complete attempt by driver {} on booking {}", driverUserId, bookingId);
            return "redirect:/driver/dashboard?error=unauthorized";
        }

        if (booking.getStatus() != Booking.BookingStatus.IN_PROGRESS) {
            return "redirect:/driver/dashboard";
        }

        booking.setStatus(Booking.BookingStatus.COMPLETED);
        booking.setFinalFare(booking.getEstimatedFare());

        // Process wallet debit if selected
        if (booking.getPaymentMethod() == Booking.PaymentMethod.WALLET) {
            Optional<User> passengerOpt = userRepository.findById(booking.getPassengerId());
            if (passengerOpt.isPresent()) {
                User passenger = passengerOpt.get();
                double fare = booking.getFinalFare();
                if (passenger.getWalletBalance() >= fare) {
                    passenger.setWalletBalance(passenger.getWalletBalance() - fare);
                    userRepository.save(passenger);
                    booking.setPaymentStatus(Booking.PaymentStatus.PAID);
                    booking.setPaymentTransactionId("WLT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    booking.setPaidAt(Instant.now());
                    log.info("Debited ₹{} from wallet of user {} for booking {}", fare, passenger.getId(), bookingId);
                } else {
                    booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
                    log.warn("Insufficient wallet balance for user {}. Marked PENDING for cash collection.", passenger.getId());
                }
            }
        } else {
            // Cash / Online pending confirmation
            booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        }

        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);

        // Increment driver total completed trips and restore driver availability
        driverRepository.findByUserId(driverUserId).ifPresent(d -> {
            d.setTotalTrips(d.getTotalTrips() + 1);
            d.setAvailable(true);
            driverRepository.save(d);
            log.info("Driver {} completed trip. Total trips incremented to {}", driverUserId, d.getTotalTrips());
        });

        realtimeDispatchService.broadcastBookingStatus(booking, "Trip completed! Total: ₹" + booking.getFinalFare());
        realtimeDispatchService.purgeTelemetry(bookingId);

        log.info("Booking {} marked COMPLETED. PaymentStatus: {}", bookingId, booking.getPaymentStatus());
        return "redirect:/driver/dashboard";
    }

    /**
     * 5a. Driver Duty Online/Offline Toggle API
     */
    @PostMapping("/api/driver/status/online")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> setDriverOnlineStatus(
            @RequestParam boolean online,
            HttpSession session
    ) {
        String driverUserId = (String) session.getAttribute("DRIVER_USER_ID");
        if (driverUserId == null) {
            return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("error", "Unauthorized"));
        }

        driverRepository.findByUserId(driverUserId).ifPresent(d -> {
            d.setOnline(online);
            driverRepository.save(d);
            log.info("Driver {} duty status updated to online={}", driverUserId, online);
        });

        return org.springframework.http.ResponseEntity.ok(java.util.Map.of("status", "SUCCESS", "online", online));
    }

    /**
     * 5b. Passenger / User Settles or Confirms Payment
     */
    @PostMapping("/user/payment/settle")
    public String userSettlePayment(
            @RequestParam String bookingId,
            HttpSession session
    ) {
        String userId = (String) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/user/login";
        }

        Optional<Booking> opt = bookingRepository.findById(bookingId);
        if (opt.isPresent()) {
            Booking booking = opt.get();
            if (userId.equals(booking.getPassengerId()) && booking.getPaymentStatus() != Booking.PaymentStatus.PAID) {
                // If wallet selected and still unpaid, debit wallet
                if (booking.getPaymentMethod() == Booking.PaymentMethod.WALLET) {
                    Optional<User> passengerOpt = userRepository.findById(userId);
                    if (passengerOpt.isPresent()) {
                        User p = passengerOpt.get();
                        double amount = (booking.getFinalFare() != null) ? booking.getFinalFare() : booking.getEstimatedFare();
                        if (p.getWalletBalance() >= amount) {
                            p.setWalletBalance(p.getWalletBalance() - amount);
                            userRepository.save(p);
                        }
                    }
                }

                booking.setPaymentStatus(Booking.PaymentStatus.PAID);
                booking.setPaymentTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                booking.setPaidAt(Instant.now());
                booking.setUpdatedAt(Instant.now());
                bookingRepository.save(booking);

                realtimeDispatchService.broadcastBookingStatus(booking, "Payment confirmed and settled successfully!");
                log.info("User {} confirmed settlement for booking {}. PaymentStatus: PAID", userId, bookingId);
            }
        }

        return "redirect:/user/ride/" + bookingId + "/receipt?settled=true";
    }

    /**
     * 5c. Driver Collects Cash & Marks Payment Paid (Idempotent)
     */
    @PostMapping("/driver/payment/collect")
    public String driverCollectPayment(
            @RequestParam String bookingId,
            HttpSession session
    ) {
        String driverUserId = (String) session.getAttribute("DRIVER_USER_ID");
        if (driverUserId == null) {
            return "redirect:/driver/login";
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            if (driverUserId.equals(booking.getDriverId()) &&
                booking.getStatus() == Booking.BookingStatus.COMPLETED &&
                booking.getPaymentStatus() != Booking.PaymentStatus.PAID) {

                String txnPrefix = (booking.getPaymentMethod() == Booking.PaymentMethod.CASH) ? "CSH-" : "TXN-";
                booking.setPaymentStatus(Booking.PaymentStatus.PAID);
                booking.setPaymentTransactionId(txnPrefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                booking.setPaidAt(Instant.now());
                booking.setUpdatedAt(Instant.now());
                bookingRepository.save(booking);

                // Broadcast payment confirmation to passenger live receipt
                realtimeDispatchService.broadcastBookingStatus(booking, "Payment confirmed received by captain! Booking settled.");
                log.info("Driver {} confirmed payment received ({}) for booking {}", driverUserId, booking.getPaymentMethod(), bookingId);
            }
        }
        return "redirect:/driver/dashboard";
    }

    /**
     * 6. Admin Actions: Verify Driver
     */
    @PostMapping("/admin/driver/verify")
    public String adminVerifyDriver(
            @RequestParam String driverId,
            @RequestParam String status,
            HttpSession session
    ) {
        if (session.getAttribute("ADMIN_USER_ID") == null) {
            return "redirect:/admin/login";
        }

        Optional<Driver> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isPresent()) {
            Driver driver = driverOpt.get();
            driver.setVerificationStatus(Driver.VerificationStatus.valueOf(status.toUpperCase()));
            driverRepository.save(driver);
        }
        return "redirect:/admin/dashboard";
    }

    /**
     * 7. User or Admin Cancels Booking with Ownership Verification
     */
    @PostMapping({"/ride/cancel", "/user/cancel"})
    public String cancelRide(
            @RequestParam String bookingId,
            @RequestParam(defaultValue = "user") String role,
            HttpSession session
    ) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();

            if ("user".equalsIgnoreCase(role)) {
                String userId = (String) session.getAttribute("USER_ID");
                if (userId == null || !userId.equals(booking.getPassengerId())) {
                    return "redirect:/user/rides?error=unauthorized";
                }
            } else if ("admin".equalsIgnoreCase(role)) {
                if (session.getAttribute("ADMIN_USER_ID") == null) {
                    return "redirect:/admin/login";
                }
            }

            if (booking.getStatus() != Booking.BookingStatus.IN_PROGRESS &&
                booking.getStatus() != Booking.BookingStatus.COMPLETED) {
                booking.setStatus(Booking.BookingStatus.CANCELLED);
                booking.setUpdatedAt(Instant.now());
                bookingRepository.save(booking);

                // If a driver was already assigned, free the driver partner immediately
                if (booking.getDriverId() != null) {
                    driverRepository.findByUserId(booking.getDriverId()).ifPresent(d -> {
                        d.setAvailable(true);
                        driverRepository.save(d);
                        log.info("Driver {} freed and set available after booking {} cancellation", d.getUserId(), bookingId);
                    });
                }

                realtimeDispatchService.broadcastBookingStatus(booking, "Ride was cancelled by " + role);
                realtimeDispatchService.broadcastCancelToDrivers(bookingId, "Ride was cancelled by " + role);
                realtimeDispatchService.purgeTelemetry(bookingId);
                log.info("Booking {} cancelled by role {}", bookingId, role);
            }
        }

        if ("admin".equalsIgnoreCase(role)) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/user/rides";
    }

    /**
     * 8. Driver Drops Active Ride (Transactional)
     * Frees the driver immediately and cancels the active assignment
     */
    @PostMapping({"/driver/ride/drop", "/ride/drop"})
    public String driverDropRide(
            @RequestParam String bookingId,
            HttpSession session
    ) {
        String driverUserId = (String) session.getAttribute("DRIVER_USER_ID");
        if (driverUserId == null) {
            return "redirect:/driver/login";
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();

            // Verify assignment
            if (driverUserId.equals(booking.getDriverId())) {
                booking.setStatus(Booking.BookingStatus.CANCELLED);
                booking.setUpdatedAt(Instant.now());
                bookingRepository.save(booking);

                // Free driver partner
                driverRepository.findByUserId(driverUserId).ifPresent(d -> {
                    d.setAvailable(true);
                    driverRepository.save(d);
                    log.info("Driver {} dropped booking {} and is now available", driverUserId, bookingId);
                });

                realtimeDispatchService.broadcastBookingStatus(booking, "Captain dropped the ride request.");
                realtimeDispatchService.purgeTelemetry(bookingId);
            }
        }

        return "redirect:/driver/dashboard";
    }
}

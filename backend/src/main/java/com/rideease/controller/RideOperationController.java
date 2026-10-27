package com.rideease.controller;

import com.rideease.model.Booking;
import com.rideease.model.Driver;
import com.rideease.model.User;
import com.rideease.model.Vehicle;
import com.rideease.repository.BookingRepository;
import com.rideease.repository.DriverRepository;
import com.rideease.repository.UserRepository;
import com.rideease.repository.VehicleRepository;
import com.rideease.service.FareCalculationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Controller
public class RideOperationController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private FareCalculationService fareCalculationService;

    // 1. Passenger Creates Real Ride Booking using actual coordinates & road distance
    @PostMapping("/user/book")
    public String bookRide(
            @RequestParam String pickupAddress,
            @RequestParam String dropoffAddress,
            @RequestParam(defaultValue = "16.4445") double pickupLat,
            @RequestParam(defaultValue = "80.5085") double pickupLon,
            @RequestParam(defaultValue = "16.5175") double dropoffLat,
            @RequestParam(defaultValue = "80.6200") double dropoffLon,
            @RequestParam(defaultValue = "24.5") double distanceKm,
            @RequestParam(defaultValue = "35") int durationMinutes,
            @RequestParam String vehicleCategory,
            @RequestParam String paymentMethod,
            HttpSession session
    ) {
        String userId = (String) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/user/login";
        }

        Vehicle.VehicleCategory category = Vehicle.VehicleCategory.valueOf(vehicleCategory.toUpperCase());
        double fare = fareCalculationService.calculateFare(category, distanceKm, durationMinutes, 1.0);

        // Generate 4-digit OTP
        String otp = String.format("%04d", new SecureRandom().nextInt(10000));

        Booking booking = new Booking();
        booking.setPassengerId(userId);
        booking.setVehicleCategory(category);
        booking.setPickupAddress(pickupAddress);
        booking.setPickupLocation(new GeoJsonPoint(pickupLon, pickupLat));
        booking.setDropoffAddress(dropoffAddress);
        booking.setDropoffLocation(new GeoJsonPoint(dropoffLon, dropoffLat));
        booking.setDistanceKm(distanceKm);
        booking.setEstimatedDurationMinutes(durationMinutes);
        booking.setEstimatedFare(fare);
        booking.setStatus(Booking.BookingStatus.SEARCHING);
        booking.setOtp(otp);
        booking.setPaymentMethod(Booking.PaymentMethod.valueOf(paymentMethod.toUpperCase()));
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        booking.setCreatedAt(Instant.now());
        booking.setUpdatedAt(Instant.now());

        Booking savedBooking = bookingRepository.save(booking);

        return "redirect:/user/ride/" + savedBooking.getId() + "/track";
    }

    // 2. Driver Accepts Ride Request
    @PostMapping("/driver/ride/accept")
    public String driverAcceptRide(
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
            booking.setDriverId(driverUserId);
            booking.setStatus(Booking.BookingStatus.ACCEPTED);
            booking.setUpdatedAt(Instant.now());
            bookingRepository.save(booking);
        }
        return "redirect:/driver/dashboard";
    }

    // 3. Driver Verifies Passenger OTP & Starts Trip
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
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            if (booking.getOtp() != null && booking.getOtp().equals(otp.trim())) {
                booking.setStatus(Booking.BookingStatus.IN_PROGRESS);
                booking.setUpdatedAt(Instant.now());
                bookingRepository.save(booking);
            }
        }
        return "redirect:/driver/dashboard";
    }

    // 4. Driver Completes Trip & Finalizes Fare
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
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus(Booking.BookingStatus.COMPLETED);
            booking.setFinalFare(booking.getEstimatedFare());
            booking.setPaymentStatus(Booking.PaymentStatus.PAID);
            booking.setUpdatedAt(Instant.now());
            bookingRepository.save(booking);
        }
        return "redirect:/driver/dashboard";
    }

    // 5. Admin Actions: Verify Driver
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

    // 6. User or Admin Cancels Booking
    @PostMapping("/ride/cancel")
    public String cancelRide(
            @RequestParam String bookingId,
            @RequestParam(defaultValue = "user") String role
    ) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            if (booking.getStatus() != Booking.BookingStatus.IN_PROGRESS &&
                booking.getStatus() != Booking.BookingStatus.COMPLETED) {
                booking.setStatus(Booking.BookingStatus.CANCELLED);
                booking.setUpdatedAt(Instant.now());
                bookingRepository.save(booking);
            }
        }
        if ("admin".equalsIgnoreCase(role)) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/user/rides";
    }
}

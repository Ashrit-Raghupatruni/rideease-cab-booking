package com.rideease.controller;

import com.rideease.model.Booking;
import com.rideease.model.Review;
import com.rideease.model.SafetyContact;
import com.rideease.model.User;
import com.rideease.repository.BookingRepository;
import com.rideease.repository.ReviewRepository;
import com.rideease.repository.SafetyContactRepository;
import com.rideease.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Competitive Product Features:
 * - Safety Centre & Emergency Contacts & SOS
 * - Rating & Reviews (Passenger <-> Driver)
 * - Downloadable Tax Invoice / Ride Receipt
 * - Saved Locations & Rebooking
 */
@Controller
public class CompetitiveProductController {

    private static final Logger log = LoggerFactory.getLogger(CompetitiveProductController.class);

    @Autowired
    private SafetyContactRepository safetyContactRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. SAFETY CENTRE VIEW
    @GetMapping("/user/safety")
    public String safetyCenter(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/user/login";
        }

        List<SafetyContact> contacts = safetyContactRepository.findByUserId(userId);
        model.addAttribute("contacts", contacts);
        return "user/safety";
    }

    // 2. ADD EMERGENCY CONTACT
    @PostMapping("/user/safety/contact/add")
    public String addEmergencyContact(
            @RequestParam String name,
            @RequestParam String phone,
            @RequestParam(defaultValue = "Family") String relationship,
            HttpSession session
    ) {
        String userId = (String) session.getAttribute("USER_ID");
        if (userId != null && !name.isBlank() && !phone.isBlank()) {
            SafetyContact contact = new SafetyContact(userId, name.trim(), phone.trim(), relationship.trim());
            safetyContactRepository.save(contact);
            log.info("Saved emergency contact for user {}: {}", userId, name);
        }
        return "redirect:/user/safety";
    }

    // 3. DELETE EMERGENCY CONTACT
    @PostMapping("/user/safety/contact/delete")
    public String deleteEmergencyContact(@RequestParam String contactId, HttpSession session) {
        String userId = (String) session.getAttribute("USER_ID");
        if (userId != null) {
            safetyContactRepository.deleteById(contactId);
        }
        return "redirect:/user/safety";
    }

    // 4. TRIGGER EMERGENCY SOS ALERT
    @PostMapping("/user/safety/sos")
    public String triggerSosAlert(@RequestParam(required = false) String bookingId, HttpSession session) {
        String userId = (String) session.getAttribute("USER_ID");
        log.warn("EMERGENCY SOS TRIGGERED by user {} for booking {}!", userId, bookingId);
        return "redirect:/user/safety?sos=triggered";
    }

    // 5. SUBMIT RIDE REVIEW & RATING
    @PostMapping("/user/ride/review")
    public String submitReview(
            @RequestParam String bookingId,
            @RequestParam int rating,
            @RequestParam(required = false) String comment,
            HttpSession session
    ) {
        String userId = (String) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/user/login";
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            Review review = new Review();
            review.setBookingId(bookingId);
            review.setPassengerId(userId);
            review.setDriverId(booking.getDriverId());
            review.setRating(Math.max(1, Math.min(5, rating)));
            review.setComment(comment != null ? comment.trim() : "");
            review.setCreatedAt(Instant.now());
            reviewRepository.save(review);
            log.info("Saved {} star review for booking {} by user {}", rating, bookingId, userId);
        }

        return "redirect:/user/rides";
    }

    // 6. DOWNLOADABLE RIDE RECEIPT / TAX INVOICE (PDF/HTML formatted text)
    @GetMapping("/user/receipt/{bookingId}")
    public ResponseEntity<byte[]> downloadRideReceipt(@PathVariable String bookingId, HttpSession session) {
        String userId = (String) session.getAttribute("USER_ID");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Booking b = bookingOpt.get();
        double fare = b.getFinalFare() != null ? b.getFinalFare() : b.getEstimatedFare();
        double baseRate = Math.round(fare * 0.82 * 100.0) / 100.0;
        double gstRate = Math.round(fare * 0.18 * 100.0) / 100.0;

        StringBuilder sb = new StringBuilder();
        sb.append("=============================================================\n");
        sb.append("                   RIDEEASE CAB SERVICES                     \n");
        sb.append("                TAX INVOICE / RIDE RECEIPT                   \n");
        sb.append("=============================================================\n");
        sb.append("Invoice No:        INV-").append(b.getId()).append("\n");
        sb.append("Trip Date:         ").append(b.getCreatedAt() != null ? b.getCreatedAt().toString() : Instant.now().toString()).append("\n");
        sb.append("Vehicle Type:      ").append(b.getVehicleCategory()).append("\n");
        sb.append("Payment Method:    ").append(b.getPaymentMethod()).append("\n");
        sb.append("Payment Status:    ").append(b.getPaymentStatus()).append("\n");
        sb.append("Transaction Ref:   ").append(b.getPaymentTransactionId() != null ? b.getPaymentTransactionId() : "N/A").append("\n");
        sb.append("-------------------------------------------------------------\n");
        sb.append("PICKUP LOCATION:   ").append(b.getPickupAddress()).append("\n");
        sb.append("DESTINATION:       ").append(b.getDropoffAddress()).append("\n");
        sb.append("ROAD DISTANCE:     ").append(b.getDistanceKm()).append(" KM\n");
        sb.append("DURATION:          ").append(b.getEstimatedDurationMinutes()).append(" MINS\n");
        sb.append("-------------------------------------------------------------\n");
        sb.append("Base Fare Amount:                    INR ").append(String.format("%.2f", baseRate)).append("\n");
        sb.append("GST @ 18% (SGST 9% + CGST 9%):        INR ").append(String.format("%.2f", gstRate)).append("\n");
        sb.append("-------------------------------------------------------------\n");
        sb.append("TOTAL NET PAID:                      INR ").append(String.format("%.2f", fare)).append("\n");
        sb.append("=============================================================\n");
        sb.append("       Thank you for choosing RideEase! Safe Travels.        \n");
        sb.append("=============================================================\n");

        byte[] receiptBytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"RideEase_Receipt_" + bookingId.substring(0, 8) + ".txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(receiptBytes);
    }
}

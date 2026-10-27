package com.rideease.controller;

import com.rideease.model.Booking;
import com.rideease.model.User;
import com.rideease.repository.BookingRepository;
import com.rideease.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class UserAccountController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // 1. Settings & Account Profile Page
    @GetMapping("/user/settings")
    public String userSettings(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/user/login";
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/user/login";
        }
        model.addAttribute("user", userOpt.get());
        return "user/settings";
    }

    // 2. Delete Account Operation
    @PostMapping("/user/account/delete")
    public String deleteUserAccount(HttpSession session) {
        String userId = (String) session.getAttribute("USER_ID");
        if (userId != null) {
            userRepository.deleteById(userId);
            session.invalidate();
        }
        return "redirect:/user/login?deleted=true";
    }

    // 3. Driver Settings Page
    @GetMapping("/driver/settings")
    public String driverSettings(HttpSession session, Model model) {
        String driverUserId = (String) session.getAttribute("DRIVER_USER_ID");
        if (driverUserId == null) {
            return "redirect:/driver/login";
        }
        Optional<User> driverUserOpt = userRepository.findById(driverUserId);
        if (driverUserOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/driver/login";
        }
        model.addAttribute("driverUser", driverUserOpt.get());
        return "driver/settings";
    }

    // 4. Delete Driver Account Operation
    @PostMapping("/driver/account/delete")
    public String deleteDriverAccount(HttpSession session) {
        String driverUserId = (String) session.getAttribute("DRIVER_USER_ID");
        if (driverUserId != null) {
            userRepository.deleteById(driverUserId);
            session.invalidate();
        }
        return "redirect:/driver/login?deleted=true";
    }

    // 5. User Payment History
    @GetMapping("/user/payments")
    public String userPaymentHistory(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/user/login";
        }
        List<Booking> myBookings = bookingRepository.findByPassengerIdOrderByCreatedAtDesc(userId);
        model.addAttribute("bookings", myBookings);
        double totalSpent = myBookings.stream()
                .filter(b -> b.getFinalFare() != null)
                .mapToDouble(Booking::getFinalFare)
                .sum();
        model.addAttribute("totalSpent", totalSpent);
        return "user/payments";
    }

    // 6. Driver Payment / Payout History
    @GetMapping("/driver/payments")
    public String driverPaymentHistory(HttpSession session, Model model) {
        String driverUserId = (String) session.getAttribute("DRIVER_USER_ID");
        if (driverUserId == null) {
            return "redirect:/driver/login";
        }
        List<Booking> driverTrips = bookingRepository.findByDriverIdOrderByCreatedAtDesc(driverUserId);
        model.addAttribute("trips", driverTrips);
        double totalEarned = driverTrips.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.COMPLETED && b.getFinalFare() != null)
                .mapToDouble(Booking::getFinalFare)
                .sum();
        model.addAttribute("totalEarned", totalEarned);
        return "driver/payments";
    }
}

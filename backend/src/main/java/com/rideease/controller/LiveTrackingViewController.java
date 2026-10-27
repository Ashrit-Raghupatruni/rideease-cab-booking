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
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class LiveTrackingViewController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user/ride/{id}/track")
    public String trackRide(@PathVariable("id") String id, HttpSession session, Model model) {
        String userId = (String) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/user/login";
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return "redirect:/user/rides";
        }

        Booking booking = bookingOpt.get();
        model.addAttribute("booking", booking);

        // If driver assigned, load driver's details
        if (booking.getDriverId() != null) {
            Optional<User> driverUserOpt = userRepository.findById(booking.getDriverId());
            model.addAttribute("driverName", driverUserOpt.map(User::getName).orElse("Captain Assigned"));
            model.addAttribute("driverPhone", driverUserOpt.map(User::getPhone).orElse("+91 98765 00000"));
        } else {
            model.addAttribute("driverName", "Searching...");
            model.addAttribute("driverPhone", "");
        }

        return "user/track";
    }
}

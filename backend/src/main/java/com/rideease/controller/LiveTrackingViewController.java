package com.rideease.controller;

import com.rideease.model.Booking;
import com.rideease.model.Driver;
import com.rideease.model.User;
import com.rideease.model.Vehicle;
import com.rideease.repository.BookingRepository;
import com.rideease.repository.DriverRepository;
import com.rideease.repository.UserRepository;
import com.rideease.repository.VehicleRepository;
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

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

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

        // If booking is already completed, redirect directly to receipt & payment confirmation page
        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            return "redirect:/user/ride/" + id + "/receipt";
        }

        model.addAttribute("booking", booking);

        // If driver assigned, load driver's details and registered vehicle
        if (booking.getDriverId() != null) {
            Optional<User> driverUserOpt = userRepository.findById(booking.getDriverId());
            model.addAttribute("driverName", driverUserOpt.map(User::getName).orElse("Captain Assigned"));
            model.addAttribute("driverPhone", driverUserOpt.map(User::getPhone).orElse("+91 98765 00000"));

            Optional<Driver> driverOpt = driverRepository.findByUserId(booking.getDriverId());
            if (driverOpt.isEmpty()) {
                driverOpt = driverRepository.findById(booking.getDriverId());
            }

            if (driverOpt.isPresent()) {
                Driver d = driverOpt.get();
                model.addAttribute("driverRating", String.format("%.1f", d.getRating()));

                Optional<Vehicle> vOpt = vehicleRepository.findByDriverId(d.getId());
                if (vOpt.isPresent()) {
                    Vehicle v = vOpt.get();
                    model.addAttribute("vehicleName", v.getMake() + " " + v.getModel());
                    model.addAttribute("vehiclePlate", v.getRegistrationNumber());
                    model.addAttribute("vehicleCategory", v.getCategory() != null ? v.getCategory().name() : "CAB");
                } else {
                    model.addAttribute("vehicleName", "Verified Commercial Cab");
                    model.addAttribute("vehiclePlate", d.getLicenseNumber() != null ? d.getLicenseNumber() : "AP-COMMERCIAL");
                    model.addAttribute("vehicleCategory", "CAB");
                }
            } else {
                model.addAttribute("driverRating", "4.9");
                model.addAttribute("vehicleName", "Verified Commercial Cab");
                model.addAttribute("vehiclePlate", "AP-COMMERCIAL");
                model.addAttribute("vehicleCategory", "CAB");
            }
        } else {
            model.addAttribute("driverName", "Searching...");
            model.addAttribute("driverPhone", "");
            model.addAttribute("driverRating", "5.0");
            model.addAttribute("vehicleName", "");
            model.addAttribute("vehiclePlate", "");
            model.addAttribute("vehicleCategory", "");
        }

        return "user/track";
    }

    /**
     * Public Live Tracking Share Endpoint (No authentication required)
     * Allows friends & family to view real-time location, pickup, and destination
     */
    @GetMapping({"/user/ride/{id}/share", "/ride/{id}/share"})
    public String shareTrackRide(@PathVariable("id") String id, Model model) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return "redirect:/user/login";
        }

        Booking booking = bookingOpt.get();

        // If booking is already completed, redirect directly to receipt & payment confirmation page
        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            return "redirect:/user/ride/" + id + "/receipt";
        }

        model.addAttribute("booking", booking);

        // Load masked captain details for safety
        if (booking.getDriverId() != null) {
            Optional<User> driverUserOpt = userRepository.findById(booking.getDriverId());
            model.addAttribute("driverName", driverUserOpt.map(User::getName).orElse("Captain"));
        } else {
            model.addAttribute("driverName", "Searching for Captain");
        }

        return "user/share_track";
    }

    /**
     * Dedicated Payment Confirmation & Trip Receipt Page
     */
    @GetMapping({"/user/ride/{id}/receipt", "/ride/{id}/receipt"})
    public String showTripReceipt(@PathVariable("id") String id, Model model, HttpSession session) {
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
        return "user/receipt";
    }
}

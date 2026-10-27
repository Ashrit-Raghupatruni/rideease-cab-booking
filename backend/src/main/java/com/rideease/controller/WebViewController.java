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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class WebViewController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FareCalculationService fareCalculationService;

    // 1. Root
    @GetMapping("/")
    public String index(HttpSession session) {
        if (session.getAttribute("USER_ID") != null) {
            return "redirect:/user/home";
        }
        return "redirect:/user/login";
    }

    // 2. USER PANEL (Port 8080)
    @GetMapping("/user/home")
    public String userHome(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/user/login";
        }

        Optional<User> userOpt = userRepository.findById(userId);
        model.addAttribute("userName", userOpt.map(User::getName).orElse("Rider"));
        model.addAttribute("userEmail", userOpt.map(User::getEmail).orElse(""));
        model.addAttribute("walletBalance", 2450.00);

        return "user/home";
    }

    @GetMapping("/user/search")
    public String userSearch(
            @RequestParam(defaultValue = "SRM University, AP") String pickup,
            @RequestParam(defaultValue = "Vijayawada Railway Station") String dropoff,
            @RequestParam(defaultValue = "16.4445") double pickupLat,
            @RequestParam(defaultValue = "80.5085") double pickupLon,
            @RequestParam(defaultValue = "16.5175") double dropoffLat,
            @RequestParam(defaultValue = "80.6200") double dropoffLon,
            @RequestParam(defaultValue = "24.5") double distanceKm,
            @RequestParam(defaultValue = "35") int durationMinutes,
            HttpSession session,
            Model model
    ) {
        if (session.getAttribute("USER_ID") == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("pickup", pickup);
        model.addAttribute("dropoff", dropoff);
        model.addAttribute("pickupLat", pickupLat);
        model.addAttribute("pickupLon", pickupLon);
        model.addAttribute("dropoffLat", dropoffLat);
        model.addAttribute("dropoffLon", dropoffLon);
        model.addAttribute("distanceKm", distanceKm);
        model.addAttribute("durationMinutes", durationMinutes);

        // Dynamically calculate fares based on actual road distance and duration
        double miniFare = fareCalculationService.calculateFare(Vehicle.VehicleCategory.MINI, distanceKm, durationMinutes, 1.0);
        double sedanFare = fareCalculationService.calculateFare(Vehicle.VehicleCategory.SEDAN, distanceKm, durationMinutes, 1.0);
        double suvFare = fareCalculationService.calculateFare(Vehicle.VehicleCategory.SUV, distanceKm, durationMinutes, 1.0);

        model.addAttribute("miniFare", miniFare);
        model.addAttribute("sedanFare", sedanFare);
        model.addAttribute("suvFare", suvFare);

        return "user/search";
    }

    @GetMapping("/user/rides")
    public String userRideHistory(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("USER_ID");
        if (userId == null) {
            return "redirect:/user/login";
        }

        List<Booking> myBookings = bookingRepository.findByPassengerIdOrderByCreatedAtDesc(userId);
        model.addAttribute("bookings", myBookings);
        model.addAttribute("userName", session.getAttribute("USER_NAME"));

        return "user/rides";
    }

    // 3. DRIVER PANEL (Port 8081)
    @GetMapping("/driver/dashboard")
    public String driverDashboard(HttpSession session, Model model) {
        String driverUserId = (String) session.getAttribute("DRIVER_USER_ID");
        if (driverUserId == null) {
            return "redirect:/driver/login";
        }

        Optional<User> driverUserOpt = userRepository.findById(driverUserId);
        model.addAttribute("driverName", driverUserOpt.map(User::getName).orElse("Driver Partner"));
        model.addAttribute("driverEmail", driverUserOpt.map(User::getEmail).orElse(""));

        List<Booking> incomingRequests = bookingRepository.findByStatus(Booking.BookingStatus.SEARCHING);
        model.addAttribute("incomingRequests", incomingRequests);

        List<Booking> myDriverTrips = bookingRepository.findByDriverIdOrderByCreatedAtDesc(driverUserId);
        model.addAttribute("myDriverTrips", myDriverTrips);

        double realEarnings = myDriverTrips.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.COMPLETED && b.getFinalFare() != null)
                .mapToDouble(Booking::getFinalFare)
                .sum();
        long completedCount = myDriverTrips.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.COMPLETED)
                .count();

        model.addAttribute("realEarnings", realEarnings);
        model.addAttribute("completedTrips", completedCount);

        return "driver/dashboard";
    }

    // 4. ADMIN PANEL (Port 8082)
    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (session.getAttribute("ADMIN_USER_ID") == null) {
            return "redirect:/admin/login";
        }

        List<User> allUsers = userRepository.findAll();
        List<Booking> allBookings = bookingRepository.findAll();
        List<Driver> allDrivers = driverRepository.findAll();

        long totalUsers = allUsers.stream().filter(u -> u.getRole() == com.rideease.model.Role.PASSENGER).count();
        long totalDrivers = allUsers.stream().filter(u -> u.getRole() == com.rideease.model.Role.DRIVER).count();
        long activeRides = allBookings.stream().filter(b -> b.getStatus() == Booking.BookingStatus.IN_PROGRESS || b.getStatus() == Booking.BookingStatus.ACCEPTED).count();

        double totalRevenue = allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.COMPLETED && b.getFinalFare() != null)
                .mapToDouble(Booking::getFinalFare)
                .sum();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalDrivers", totalDrivers);
        model.addAttribute("activeRides", activeRides);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("allBookings", allBookings);
        model.addAttribute("allUsers", allUsers);
        model.addAttribute("allDrivers", allDrivers);

        return "admin/dashboard";
    }
}

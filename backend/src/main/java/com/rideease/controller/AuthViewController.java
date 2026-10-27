package com.rideease.controller;

import com.rideease.model.Role;
import com.rideease.model.User;
import com.rideease.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.Optional;

@Controller
public class AuthViewController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- USER AUTHENTICATION ---
    @GetMapping("/user/login")
    public String userLoginPage(HttpSession session, Model model) {
        if (session.getAttribute("USER_ID") != null) {
            return "redirect:/user/home";
        }
        return "user/login";
    }

    @PostMapping("/user/login")
    public String processUserLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            User user = userOpt.get();
            session.setAttribute("USER_ID", user.getId());
            session.setAttribute("USER_NAME", user.getName());
            session.setAttribute("USER_ROLE", user.getRole().name());
            return "redirect:/user/home";
        }
        model.addAttribute("error", "Invalid email or password.");
        return "user/login";
    }

    @GetMapping("/user/signup")
    public String userSignupPage(HttpSession session) {
        if (session.getAttribute("USER_ID") != null) {
            return "redirect:/user/home";
        }
        return "user/signup";
    }

    @PostMapping("/user/signup")
    public String processUserSignup(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Email is already registered. Please login.");
            return "user/signup";
        }
        User newUser = new User(
                null,
                name,
                email,
                phone,
                passwordEncoder.encode(password),
                Role.PASSENGER,
                null,
                true,
                Instant.now(),
                Instant.now()
        );
        userRepository.save(newUser);

        session.setAttribute("USER_ID", newUser.getId());
        session.setAttribute("USER_NAME", newUser.getName());
        session.setAttribute("USER_ROLE", Role.PASSENGER.name());

        return "redirect:/user/home";
    }

    // --- DRIVER AUTHENTICATION ---
    @GetMapping("/driver/login")
    public String driverLoginPage(HttpSession session) {
        if (session.getAttribute("DRIVER_USER_ID") != null) {
            return "redirect:/driver/dashboard";
        }
        return "driver/login";
    }

    @PostMapping("/driver/login")
    public String processDriverLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent() && userOpt.get().getRole() == Role.DRIVER &&
                passwordEncoder.matches(password, userOpt.get().getPassword())) {
            User driverUser = userOpt.get();
            session.setAttribute("DRIVER_USER_ID", driverUser.getId());
            session.setAttribute("DRIVER_NAME", driverUser.getName());
            return "redirect:/driver/dashboard";
        }
        model.addAttribute("error", "Invalid driver credentials. Ensure you have registered as Driver.");
        return "driver/login";
    }

    @GetMapping("/driver/signup")
    public String driverSignupPage() {
        return "driver/signup";
    }

    @PostMapping("/driver/signup")
    public String processDriverSignup(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Email is already registered.");
            return "driver/signup";
        }

        User newDriverUser = new User(
                null,
                name,
                email,
                phone,
                passwordEncoder.encode(password),
                Role.DRIVER,
                null,
                true,
                Instant.now(),
                Instant.now()
        );
        userRepository.save(newDriverUser);

        session.setAttribute("DRIVER_USER_ID", newDriverUser.getId());
        session.setAttribute("DRIVER_NAME", newDriverUser.getName());
        return "redirect:/driver/dashboard";
    }

    // --- ADMIN AUTHENTICATION ---
    @GetMapping("/admin/login")
    public String adminLoginPage(HttpSession session) {
        if (session.getAttribute("ADMIN_USER_ID") != null) {
            return "redirect:/admin/dashboard";
        }
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String processAdminLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent() && userOpt.get().getRole() == Role.ADMIN &&
                passwordEncoder.matches(password, userOpt.get().getPassword())) {
            session.setAttribute("ADMIN_USER_ID", userOpt.get().getId());
            session.setAttribute("ADMIN_NAME", userOpt.get().getName());
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("error", "Unauthorized: Incorrect admin credentials.");
        return "admin/login";
    }

    // --- LOGOUT ---
    @GetMapping("/auth/logout")
    public String logout(HttpSession session, @RequestParam(defaultValue = "user") String type) {
        session.invalidate();
        if ("driver".equalsIgnoreCase(type)) {
            return "redirect:http://localhost:8081/driver/login";
        } else if ("admin".equalsIgnoreCase(type)) {
            return "redirect:http://localhost:8082/admin/login";
        }
        return "redirect:http://localhost:8080/user/login";
    }
}

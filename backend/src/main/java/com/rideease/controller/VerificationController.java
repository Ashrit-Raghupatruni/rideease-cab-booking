package com.rideease.controller;

import com.rideease.model.Driver;
import com.rideease.model.User;
import com.rideease.model.Vehicle;
import com.rideease.repository.DriverRepository;
import com.rideease.repository.UserRepository;
import com.rideease.repository.VehicleRepository;
import com.rideease.service.MediaStorageService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.UUID;

@Controller
public class VerificationController {

    private static final Logger log = LoggerFactory.getLogger(VerificationController.class);

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MediaStorageService mediaStorageService;

    // --- DRIVER ONBOARDING & VERIFICATION UPLOAD ---

    @GetMapping("/driver/verification")
    public String driverVerificationPage(HttpSession session, Model model) {
        String driverUserId = (String) session.getAttribute("DRIVER_USER_ID");
        if (driverUserId == null) {
            return "redirect:/driver/login";
        }

        Optional<User> userOpt = userRepository.findById(driverUserId);
        Optional<Driver> driverOpt = driverRepository.findByUserId(driverUserId);
        Optional<Vehicle> vehicleOpt = driverOpt.flatMap(d -> vehicleRepository.findByDriverId(d.getId()));

        model.addAttribute("driverUser", userOpt.orElse(null));
        model.addAttribute("driver", driverOpt.orElse(null));
        model.addAttribute("vehicle", vehicleOpt.orElse(null));

        return "driver/verification";
    }

    @PostMapping("/driver/verification/submit")
    public String submitDriverVerification(
            @RequestParam(value = "driverPhoto", required = false) MultipartFile driverPhoto,
            @RequestParam(value = "licensePhoto", required = false) MultipartFile licensePhoto,
            @RequestParam("licenseNumber") String licenseNumber,
            @RequestParam("vehicleCategory") Vehicle.VehicleCategory vehicleCategory,
            @RequestParam("make") String make,
            @RequestParam("model") String modelName,
            @RequestParam("color") String color,
            @RequestParam(value = "manufacturingYear", defaultValue = "2022") Integer manufacturingYear,
            @RequestParam("registrationNumber") String registrationNumber,
            @RequestParam(value = "vehiclePhoto", required = false) MultipartFile vehiclePhoto,
            @RequestParam(value = "rcBook", required = false) MultipartFile rcBook,
            @RequestParam(value = "insurancePolicy", required = false) MultipartFile insurancePolicy,
            @RequestParam(value = "fitnessCertificate", required = false) MultipartFile fitnessCertificate,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String driverUserId = (String) session.getAttribute("DRIVER_USER_ID");
        if (driverUserId == null) {
            return "redirect:/driver/login";
        }

        try {
            // 1. Fetch or create Driver entity
            Driver driver = driverRepository.findByUserId(driverUserId).orElseGet(() -> {
                Driver d = new Driver();
                d.setId("DRV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                d.setUserId(driverUserId);
                d.setVerificationStatus(Driver.VerificationStatus.PENDING);
                return d;
            });

            driver.setLicenseNumber(licenseNumber.trim());

            // Upload Driver Profile Photo to Cloudinary
            if (driverPhoto != null && !driverPhoto.isEmpty()) {
                String photoUrl = mediaStorageService.uploadDriverPhoto(driverPhoto, driver.getId());
                driver.setDriverPhotoUrl(photoUrl);
            }

            // Upload License Photo to Cloudinary
            if (licensePhoto != null && !licensePhoto.isEmpty()) {
                String licenseUrl = mediaStorageService.uploadImage(licensePhoto, "drivers/" + driver.getId() + "/license");
                driver.setLicensePhotoUrl(licenseUrl);
            }

            driver.setVerificationStatus(Driver.VerificationStatus.PENDING);
            driver.setVerificationNotes("Documents submitted for administrative review.");
            Driver savedDriver = driverRepository.save(driver);

            // 2. Fetch or create Vehicle entity
            Vehicle vehicle = vehicleRepository.findByDriverId(savedDriver.getId()).orElseGet(() -> {
                Vehicle v = new Vehicle();
                v.setId("VEH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                v.setDriverId(savedDriver.getId());
                v.setVerificationStatus(Vehicle.VerificationStatus.PENDING);
                return v;
            });

            vehicle.setCategory(vehicleCategory);
            vehicle.setMake(make.trim());
            vehicle.setModel(modelName.trim());
            vehicle.setColor(color.trim());
            vehicle.setManufacturingYear(manufacturingYear);
            vehicle.setRegistrationNumber(registrationNumber.trim().toUpperCase());

            // Upload Vehicle Photo to Cloudinary
            if (vehiclePhoto != null && !vehiclePhoto.isEmpty()) {
                String vehPhotoUrl = mediaStorageService.uploadVehiclePhoto(vehiclePhoto, vehicle.getId());
                vehicle.setVehiclePhotoUrl(vehPhotoUrl);
            }

            // Upload RC Book (Registration Certificate) to Cloudinary
            if (rcBook != null && !rcBook.isEmpty()) {
                String rcUrl = mediaStorageService.uploadVehicleDocument(rcBook, "rc_book", vehicle.getId());
                vehicle.setRcBookUrl(rcUrl);
                driver.setRcDocumentUrl(rcUrl);
            }

            // Upload Insurance Policy to Cloudinary
            if (insurancePolicy != null && !insurancePolicy.isEmpty()) {
                String insUrl = mediaStorageService.uploadVehicleDocument(insurancePolicy, "insurance", vehicle.getId());
                vehicle.setInsurancePolicyUrl(insUrl);
                driver.setInsuranceDocumentUrl(insUrl);
            }

            // Upload Fitness/Permit to Cloudinary
            if (fitnessCertificate != null && !fitnessCertificate.isEmpty()) {
                String fitUrl = mediaStorageService.uploadVehicleDocument(fitnessCertificate, "permit", vehicle.getId());
                vehicle.setFitnessCertificateUrl(fitUrl);
                driver.setPermitDocumentUrl(fitUrl);
            }

            vehicle.setVerificationStatus(Vehicle.VerificationStatus.PENDING);
            vehicleRepository.save(vehicle);
            driverRepository.save(driver);

            redirectAttributes.addFlashAttribute("success", "Vehicle & driver documents uploaded to Cloudinary! Under verification review.");
            return "redirect:/driver/verification";

        } catch (Exception e) {
            log.error("Failed to upload driver verification documents: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Document upload failed: " + e.getMessage());
            return "redirect:/driver/verification";
        }
    }

    // --- ADMIN VERIFICATION WORKFLOW ---

    @GetMapping("/admin/verifications")
    public String adminVerificationsPage(HttpSession session, Model model) {
        String adminEmail = (String) session.getAttribute("ADMIN_EMAIL");
        if (adminEmail == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("pendingDrivers", driverRepository.findByVerificationStatus(Driver.VerificationStatus.PENDING));
        model.addAttribute("verifiedDrivers", driverRepository.findByVerificationStatus(Driver.VerificationStatus.APPROVED));
        model.addAttribute("allVehicles", vehicleRepository.findAll());

        // Map userId to User object for full driver names and emails
        java.util.Map<String, User> userMap = userRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        model.addAttribute("userMap", userMap);

        return "admin/verifications";
    }

    @PostMapping("/admin/verification/action")
    public String adminProcessVerification(
            @RequestParam("driverId") String driverId,
            @RequestParam("action") String action, // "APPROVE" or "REJECT"
            @RequestParam(value = "reason", required = false) String reason,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String adminEmail = (String) session.getAttribute("ADMIN_EMAIL");
        if (adminEmail == null) {
            return "redirect:/admin/login";
        }

        Optional<Driver> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Driver not found.");
            return "redirect:/admin/verifications";
        }

        Driver driver = driverOpt.get();
        Optional<Vehicle> vehicleOpt = vehicleRepository.findByDriverId(driver.getId());

        if ("APPROVE".equalsIgnoreCase(action)) {
            driver.setVerificationStatus(Driver.VerificationStatus.APPROVED);
            driver.setVerificationNotes("Verified and approved by Administrator (" + adminEmail + ")");
            driverRepository.save(driver);

            vehicleOpt.ifPresent(v -> {
                v.setVerificationStatus(Vehicle.VerificationStatus.APPROVED);
                v.setRejectionReason(null);
                vehicleRepository.save(v);
            });

            redirectAttributes.addFlashAttribute("success", "Driver & Vehicle approved successfully!");
        } else {
            driver.setVerificationStatus(Driver.VerificationStatus.REJECTED);
            driver.setVerificationNotes(reason != null && !reason.isBlank() ? reason : "Documents failed verification criteria.");
            driverRepository.save(driver);

            vehicleOpt.ifPresent(v -> {
                v.setVerificationStatus(Vehicle.VerificationStatus.REJECTED);
                v.setRejectionReason(reason);
                vehicleRepository.save(v);
            });

            redirectAttributes.addFlashAttribute("warning", "Driver & Vehicle verification rejected.");
        }

        return "redirect:/admin/verifications";
    }
}

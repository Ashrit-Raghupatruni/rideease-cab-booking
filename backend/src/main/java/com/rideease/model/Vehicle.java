package com.rideease.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicles", indexes = {
    @Index(name = "idx_vehicle_driver_id", columnList = "driverId", unique = true),
    @Index(name = "idx_vehicle_reg_num", columnList = "registrationNumber", unique = true)
})
public class Vehicle {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, unique = true, length = 64)
    private String driverId;

    @Enumerated(EnumType.STRING)
    private VehicleCategory category; // MINI, SEDAN, SUV

    private String make;
    private String model;
    private String color;
    private Integer manufacturingYear;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    // Vehicle photo stored in Cloudinary
    private String vehiclePhotoUrl;

    // Specific vehicle documents stored in Cloudinary
    private String rcBookUrl;
    private String insurancePolicyUrl;
    private String fitnessCertificateUrl;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    private String rejectionReason;

    public Vehicle() {}

    public Vehicle(String id, String driverId, VehicleCategory category, String make, String model,
                   String color, String registrationNumber, String vehiclePhotoUrl) {
        this.id = id;
        this.driverId = driverId;
        this.category = category;
        this.make = make;
        this.model = model;
        this.color = color;
        this.registrationNumber = registrationNumber;
        this.vehiclePhotoUrl = vehiclePhotoUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public VehicleCategory getCategory() { return category; }
    public void setCategory(VehicleCategory category) { this.category = category; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getManufacturingYear() { return manufacturingYear; }
    public void setManufacturingYear(Integer manufacturingYear) { this.manufacturingYear = manufacturingYear; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getVehiclePhotoUrl() { return vehiclePhotoUrl; }
    public void setVehiclePhotoUrl(String vehiclePhotoUrl) { this.vehiclePhotoUrl = vehiclePhotoUrl; }

    public String getRcBookUrl() { return rcBookUrl; }
    public void setRcBookUrl(String rcBookUrl) { this.rcBookUrl = rcBookUrl; }

    public String getInsurancePolicyUrl() { return insurancePolicyUrl; }
    public void setInsurancePolicyUrl(String insurancePolicyUrl) { this.insurancePolicyUrl = insurancePolicyUrl; }

    public String getFitnessCertificateUrl() { return fitnessCertificateUrl; }
    public void setFitnessCertificateUrl(String fitnessCertificateUrl) { this.fitnessCertificateUrl = fitnessCertificateUrl; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public enum VehicleCategory {
        MINI,
        SEDAN,
        SUV
    }

    public enum VerificationStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            this.id = java.util.UUID.randomUUID().toString();
        }
    }
}

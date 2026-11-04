package com.rideease.model;

import jakarta.persistence.*;

@Entity
@Table(name = "drivers", indexes = {
    @Index(name = "idx_driver_user_id", columnList = "userId", unique = true)
})
public class Driver {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, unique = true, length = 64)
    private String userId;

    private String licenseNumber;
    private String licensePhotoUrl;

    // Driver personal profile photo (Cloudinary URL)
    private String driverPhotoUrl;

    // Verification documents (Cloudinary URLs)
    private String rcDocumentUrl;
    private String insuranceDocumentUrl;
    private String permitDocumentUrl;

    private String verificationNotes;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    private boolean online = false;
    private boolean available = true;

    // Coordinates: stored as latitude & longitude doubles for SQL
    private Double currentLatitude;
    private Double currentLongitude;

    private double rating = 5.0;
    private int totalTrips = 0;

    public Driver() {}

    public Driver(String id, String userId, String licenseNumber, String licensePhotoUrl,
                  VerificationStatus verificationStatus, boolean online, boolean available,
                  Double currentLatitude, Double currentLongitude, double rating, int totalTrips) {
        this.id = id;
        this.userId = userId;
        this.licenseNumber = licenseNumber;
        this.licensePhotoUrl = licensePhotoUrl;
        this.verificationStatus = verificationStatus != null ? verificationStatus : VerificationStatus.PENDING;
        this.online = online;
        this.available = available;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.rating = rating;
        this.totalTrips = totalTrips;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getLicensePhotoUrl() { return licensePhotoUrl; }
    public void setLicensePhotoUrl(String licensePhotoUrl) { this.licensePhotoUrl = licensePhotoUrl; }

    public String getDriverPhotoUrl() { return driverPhotoUrl; }
    public void setDriverPhotoUrl(String driverPhotoUrl) { this.driverPhotoUrl = driverPhotoUrl; }

    public String getRcDocumentUrl() { return rcDocumentUrl; }
    public void setRcDocumentUrl(String rcDocumentUrl) { this.rcDocumentUrl = rcDocumentUrl; }

    public String getInsuranceDocumentUrl() { return insuranceDocumentUrl; }
    public void setInsuranceDocumentUrl(String insuranceDocumentUrl) { this.insuranceDocumentUrl = insuranceDocumentUrl; }

    public String getPermitDocumentUrl() { return permitDocumentUrl; }
    public void setPermitDocumentUrl(String permitDocumentUrl) { this.permitDocumentUrl = permitDocumentUrl; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double currentLatitude) { this.currentLatitude = currentLatitude; }

    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double currentLongitude) { this.currentLongitude = currentLongitude; }

    /**
     * Map representation for backward compatibility with telemetry/templates accessing:
     * driver.currentLocation.coordinates[0] (lon) and coordinates[1] (lat)
     */
    public java.util.Map<String, Object> getCurrentLocation() {
        if (currentLongitude == null || currentLatitude == null) return null;
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("coordinates", new Double[]{currentLongitude, currentLatitude});
        return map;
    }

    public void setCurrentLocation(Double longitude, Double latitude) {
        this.currentLongitude = longitude;
        this.currentLatitude = latitude;
    }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getTotalTrips() { return totalTrips; }
    public void setTotalTrips(int totalTrips) { this.totalTrips = totalTrips; }

    public enum VerificationStatus {
        PENDING,
        APPROVED,
        REJECTED,
        SUSPENDED
    }

    @PrePersist
    protected void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            this.id = java.util.UUID.randomUUID().toString();
        }
    }
}

package com.rideease.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "bookings", indexes = {
    @Index(name = "idx_booking_passenger", columnList = "passengerId"),
    @Index(name = "idx_booking_driver", columnList = "driverId"),
    @Index(name = "idx_booking_status", columnList = "status")
})
public class Booking {

    @Id
    @Column(length = 64)
    private String id;

    @Column(length = 64)
    private String passengerId;

    @Column(length = 64)
    private String driverId;

    @Enumerated(EnumType.STRING)
    private Vehicle.VehicleCategory vehicleCategory;

    // Pickup details
    @Column(length = 500)
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;

    // Dropoff details
    @Column(length = 500)
    private String dropoffAddress;
    private Double dropoffLatitude;
    private Double dropoffLongitude;

    private double distanceKm;
    private int estimatedDurationMinutes;

    private double estimatedFare;
    private Double finalFare;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.REQUESTED;

    private String otp; // Verification OTP for ride start
    private int otpAttempts = 0; // Track failed verification attempts

    // Scheduled Rides Support
    private boolean scheduled = false;
    private Instant scheduledTime;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String paymentTransactionId;
    private Instant paidAt;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            this.id = java.util.UUID.randomUUID().toString();
        }
        if (this.createdAt == null) this.createdAt = Instant.now();
        if (this.updatedAt == null) this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Booking() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPassengerId() { return passengerId; }
    public void setPassengerId(String passengerId) { this.passengerId = passengerId; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public Vehicle.VehicleCategory getVehicleCategory() { return vehicleCategory; }
    public void setVehicleCategory(Vehicle.VehicleCategory vehicleCategory) { this.vehicleCategory = vehicleCategory; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public Double getPickupLatitude() { return pickupLatitude; }
    public void setPickupLatitude(Double pickupLatitude) { this.pickupLatitude = pickupLatitude; }

    public Double getPickupLongitude() { return pickupLongitude; }
    public void setPickupLongitude(Double pickupLongitude) { this.pickupLongitude = pickupLongitude; }

    /**
     * Map representation for backward compatibility with templates accessing:
     * booking.pickupLocation.coordinates[0] (lon) and coordinates[1] (lat)
     */
    public java.util.Map<String, Object> getPickupLocation() {
        if (pickupLongitude == null || pickupLatitude == null) return null;
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("coordinates", new Double[]{pickupLongitude, pickupLatitude});
        return map;
    }

    public String getDropoffAddress() { return dropoffAddress; }
    public void setDropoffAddress(String dropoffAddress) { this.dropoffAddress = dropoffAddress; }

    public Double getDropoffLatitude() { return dropoffLatitude; }
    public void setDropoffLatitude(Double dropoffLatitude) { this.dropoffLatitude = dropoffLatitude; }

    public Double getDropoffLongitude() { return dropoffLongitude; }
    public void setDropoffLongitude(Double dropoffLongitude) { this.dropoffLongitude = dropoffLongitude; }

    /**
     * Map representation for backward compatibility with templates accessing:
     * booking.dropoffLocation.coordinates[0] (lon) and coordinates[1] (lat)
     */
    public java.util.Map<String, Object> getDropoffLocation() {
        if (dropoffLongitude == null || dropoffLatitude == null) return null;
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("coordinates", new Double[]{dropoffLongitude, dropoffLatitude});
        return map;
    }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    /**
     * Convenient getter alias for templates: durationMinutes -> estimatedDurationMinutes.
     */
    public int getDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public double getEstimatedFare() { return estimatedFare; }
    public void setEstimatedFare(double estimatedFare) { this.estimatedFare = estimatedFare; }

    public Double getFinalFare() { return finalFare; }
    public void setFinalFare(Double finalFare) { this.finalFare = finalFare; }

    /**
     * Convenient getter alias for templates: trip.fare -> finalFare (if present) or estimatedFare
     */
    public double getFare() {
        return finalFare != null ? finalFare : estimatedFare;
    }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public int getOtpAttempts() { return otpAttempts; }
    public void setOtpAttempts(int otpAttempts) { this.otpAttempts = otpAttempts; }

    public boolean isScheduled() { return scheduled; }
    public void setScheduled(boolean scheduled) { this.scheduled = scheduled; }

    public Instant getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(Instant scheduledTime) { this.scheduledTime = scheduledTime; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentTransactionId() { return paymentTransactionId; }
    public void setPaymentTransactionId(String paymentTransactionId) { this.paymentTransactionId = paymentTransactionId; }

    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public enum BookingStatus {
        SCHEDULED,
        REQUESTED,
        SEARCHING,
        ACCEPTED,
        ARRIVING,
        ARRIVED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    public enum PaymentStatus {
        PENDING,
        PAID,
        FAILED,
        REFUNDED
    }

    public enum PaymentMethod {
        CASH,
        WALLET,
        UPI,
        CARD
    }
}

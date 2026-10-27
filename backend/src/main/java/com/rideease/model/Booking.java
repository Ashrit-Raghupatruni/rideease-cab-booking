package com.rideease.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "bookings")
public class Booking {

    @Id
    private String id;

    private String passengerId;
    private String driverId;
    private Vehicle.VehicleCategory vehicleCategory;

    // Pickup details
    private String pickupAddress;
    private GeoJsonPoint pickupLocation; // [lon, lat]

    // Dropoff details
    private String dropoffAddress;
    private GeoJsonPoint dropoffLocation; // [lon, lat]

    private double distanceKm;
    private int estimatedDurationMinutes;

    private double estimatedFare;
    private Double finalFare;

    private BookingStatus status = BookingStatus.REQUESTED;

    private String otp; // Verification OTP for ride start
    private int otpAttempts = 0; // Track failed verification attempts

    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private String paymentTransactionId;
    private Instant paidAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

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

    public GeoJsonPoint getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(GeoJsonPoint pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getDropoffAddress() { return dropoffAddress; }
    public void setDropoffAddress(String dropoffAddress) { this.dropoffAddress = dropoffAddress; }

    public GeoJsonPoint getDropoffLocation() { return dropoffLocation; }
    public void setDropoffLocation(GeoJsonPoint dropoffLocation) { this.dropoffLocation = dropoffLocation; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public double getEstimatedFare() { return estimatedFare; }
    public void setEstimatedFare(double estimatedFare) { this.estimatedFare = estimatedFare; }

    public Double getFinalFare() { return finalFare; }
    public void setFinalFare(Double finalFare) { this.finalFare = finalFare; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public int getOtpAttempts() { return otpAttempts; }
    public void setOtpAttempts(int otpAttempts) { this.otpAttempts = otpAttempts; }

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
        REQUESTED,
        SEARCHING,
        ACCEPTED,
        ARRIVING,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED,
        NO_DRIVER_FOUND
    }

    public enum PaymentStatus {
        PENDING,
        PAID,
        FAILED
    }

    public enum PaymentMethod {
        CASH,
        ONLINE,
        WALLET
    }
}

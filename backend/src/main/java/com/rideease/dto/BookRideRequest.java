package com.rideease.dto;

import com.rideease.model.Booking;
import com.rideease.model.Vehicle;
import jakarta.validation.constraints.*;

public class BookRideRequest {

    @NotBlank(message = "Pickup address cannot be empty")
    @Size(max = 250, message = "Pickup address must not exceed 250 characters")
    private String pickupAddress;

    @NotBlank(message = "Dropoff address cannot be empty")
    @Size(max = 250, message = "Dropoff address must not exceed 250 characters")
    private String dropoffAddress;

    @DecimalMin(value = "-90.0", message = "Pickup latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Pickup latitude must be <= 90.0")
    private double pickupLat;

    @DecimalMin(value = "-180.0", message = "Pickup longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Pickup longitude must be <= 180.0")
    private double pickupLon;

    @DecimalMin(value = "-90.0", message = "Dropoff latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Dropoff latitude must be <= 90.0")
    private double dropoffLat;

    @DecimalMin(value = "-180.0", message = "Dropoff longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Dropoff longitude must be <= 180.0")
    private double dropoffLon;

    @NotNull(message = "Vehicle category is required")
    private Vehicle.VehicleCategory vehicleCategory;

    @NotNull(message = "Payment method is required")
    private Booking.PaymentMethod paymentMethod;

    public BookRideRequest() {}

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getDropoffAddress() { return dropoffAddress; }
    public void setDropoffAddress(String dropoffAddress) { this.dropoffAddress = dropoffAddress; }

    public double getPickupLat() { return pickupLat; }
    public void setPickupLat(double pickupLat) { this.pickupLat = pickupLat; }

    public double getPickupLon() { return pickupLon; }
    public void setPickupLon(double pickupLon) { this.pickupLon = pickupLon; }

    public double getDropoffLat() { return dropoffLat; }
    public void setDropoffLat(double dropoffLat) { this.dropoffLat = dropoffLat; }

    public double getDropoffLon() { return dropoffLon; }
    public void setDropoffLon(double dropoffLon) { this.dropoffLon = dropoffLon; }

    public Vehicle.VehicleCategory getVehicleCategory() { return vehicleCategory; }
    public void setVehicleCategory(Vehicle.VehicleCategory vehicleCategory) { this.vehicleCategory = vehicleCategory; }

    public Booking.PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(Booking.PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
}

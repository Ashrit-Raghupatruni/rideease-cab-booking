package com.rideease.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "vehicles")
public class Vehicle {

    @Id
    private String id;

    @Indexed(unique = true)
    private String driverId;

    private VehicleCategory category; // MINI, SEDAN, SUV
    private String make;
    private String model;
    private String color;

    @Indexed(unique = true)
    private String registrationNumber;

    private String vehiclePhotoUrl;

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

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getVehiclePhotoUrl() { return vehiclePhotoUrl; }
    public void setVehiclePhotoUrl(String vehiclePhotoUrl) { this.vehiclePhotoUrl = vehiclePhotoUrl; }

    public enum VehicleCategory {
        MINI,
        SEDAN,
        SUV
    }
}

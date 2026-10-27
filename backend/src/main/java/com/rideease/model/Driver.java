package com.rideease.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "drivers")
public class Driver {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private String licenseNumber;
    private String licensePhotoUrl;

    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    private boolean online = false;
    private boolean available = true;

    // MongoDB 2dsphere index: stores [longitude, latitude]
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint currentLocation;

    private double rating = 5.0;
    private int totalTrips = 0;

    public Driver() {}

    public Driver(String id, String userId, String licenseNumber, String licensePhotoUrl,
                  VerificationStatus verificationStatus, boolean online, boolean available,
                  GeoJsonPoint currentLocation, double rating, int totalTrips) {
        this.id = id;
        this.userId = userId;
        this.licenseNumber = licenseNumber;
        this.licensePhotoUrl = licensePhotoUrl;
        this.verificationStatus = verificationStatus != null ? verificationStatus : VerificationStatus.PENDING;
        this.online = online;
        this.available = available;
        this.currentLocation = currentLocation;
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

    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public GeoJsonPoint getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(GeoJsonPoint currentLocation) { this.currentLocation = currentLocation; }

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
}

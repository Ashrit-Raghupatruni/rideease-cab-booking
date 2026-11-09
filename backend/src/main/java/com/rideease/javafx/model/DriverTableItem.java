package com.rideease.javafx.model;

import javafx.beans.property.*;

public class DriverTableItem {

    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty licenseNumber = new SimpleStringProperty();
    private final StringProperty vehicleCategory = new SimpleStringProperty();
    private final StringProperty vehicleModel = new SimpleStringProperty();
    private final StringProperty registrationNumber = new SimpleStringProperty();
    private final StringProperty verificationStatus = new SimpleStringProperty();
    private final StringProperty photoUrl = new SimpleStringProperty();
    private final StringProperty rcDocUrl = new SimpleStringProperty();
    private final StringProperty insuranceDocUrl = new SimpleStringProperty();
    private final StringProperty permitDocUrl = new SimpleStringProperty();
    private final DoubleProperty rating = new SimpleDoubleProperty();
    private final IntegerProperty totalTrips = new IntegerPropertyBase() {
        @Override
        public Object getBean() { return DriverTableItem.this; }
        @Override
        public String getName() { return "totalTrips"; }
    };

    public DriverTableItem(String id, String name, String licenseNumber, String category,
                           String vehicleModel, String registrationNumber, String status,
                           String photoUrl, String rcDocUrl, String insuranceDocUrl,
                           String permitDocUrl, double rating, int totalTrips) {
        this.id.set(id);
        this.name.set(name);
        this.licenseNumber.set(licenseNumber);
        this.vehicleCategory.set(category);
        this.vehicleModel.set(vehicleModel);
        this.registrationNumber.set(registrationNumber);
        this.verificationStatus.set(status);
        this.photoUrl.set(photoUrl);
        this.rcDocUrl.set(rcDocUrl);
        this.insuranceDocUrl.set(insuranceDocUrl);
        this.permitDocUrl.set(permitDocUrl);
        this.rating.set(rating);
        this.totalTrips.set(totalTrips);
    }

    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public String getLicenseNumber() { return licenseNumber.get(); }
    public StringProperty licenseNumberProperty() { return licenseNumber; }

    public String getVehicleCategory() { return vehicleCategory.get(); }
    public String getCategory() { return vehicleCategory.get(); }
    public StringProperty vehicleCategoryProperty() { return vehicleCategory; }

    public String getVehicleModel() { return vehicleModel.get(); }
    public StringProperty vehicleModelProperty() { return vehicleModel; }

    public String getRegistrationNumber() { return registrationNumber.get(); }
    public StringProperty registrationNumberProperty() { return registrationNumber; }

    public String getVerificationStatus() { return verificationStatus.get(); }
    public void setVerificationStatus(String status) { this.verificationStatus.set(status); }
    public StringProperty verificationStatusProperty() { return verificationStatus; }

    public String getPhotoUrl() { return photoUrl.get(); }
    public StringProperty photoUrlProperty() { return photoUrl; }

    public String getRcDocUrl() { return rcDocUrl.get(); }
    public StringProperty rcDocUrlProperty() { return rcDocUrl; }

    public String getInsuranceDocUrl() { return insuranceDocUrl.get(); }
    public StringProperty insuranceDocUrlProperty() { return insuranceDocUrl; }

    public String getPermitDocUrl() { return permitDocUrl.get(); }
    public StringProperty permitDocUrlProperty() { return permitDocUrl; }

    public double getRating() { return rating.get(); }
    public DoubleProperty ratingProperty() { return rating; }

    public int getTotalTrips() { return totalTrips.get(); }
    public IntegerProperty totalTripsProperty() { return totalTrips; }
}

package com.rideease.service;

import com.rideease.model.Driver;
import com.rideease.model.Vehicle;
import com.rideease.repository.DriverRepository;
import com.rideease.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DriverMatchingService {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    private static final double DEFAULT_SEARCH_RADIUS_KM = 8.0;

    /**
     * Find nearby verified & online drivers with matching vehicle category
     * @param longitude Pickup longitude
     * @param latitude Pickup latitude
     * @param category Required vehicle category
     * @return List of matching drivers
     */
    public List<Driver> findNearbyMatchingDrivers(double longitude, double latitude, Vehicle.VehicleCategory category) {
        Point pickupPoint = new Point(longitude, latitude);
        Distance searchRadius = new Distance(DEFAULT_SEARCH_RADIUS_KM, Metrics.KILOMETERS);

        // Geospatial query utilizing MongoDB 2dsphere index
        List<Driver> nearbyDrivers = driverRepository.findByVerificationStatusAndOnlineTrueAndAvailableTrueAndCurrentLocationNear(
                Driver.VerificationStatus.APPROVED,
                pickupPoint,
                searchRadius
        );

        // Filter drivers whose vehicle matches the requested vehicle category
        return nearbyDrivers.stream().filter(driver -> {
            Optional<Vehicle> vehicleOpt = vehicleRepository.findByDriverId(driver.getId());
            return vehicleOpt.isPresent() && vehicleOpt.get().getCategory() == category;
        }).collect(Collectors.toList());
    }
}

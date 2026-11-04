package com.rideease.service;

import com.rideease.model.Driver;
import com.rideease.model.Vehicle;
import com.rideease.repository.DriverRepository;
import com.rideease.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final double DEFAULT_SEARCH_RADIUS_KM = 12.0;

    /**
     * Find nearby verified & online drivers with matching vehicle category
     * Uses Haversine spherical distance calculation
     * @param longitude Pickup longitude
     * @param latitude Pickup latitude
     * @param category Required vehicle category
     * @return List of matching drivers
     */
    public List<Driver> findNearbyMatchingDrivers(double longitude, double latitude, Vehicle.VehicleCategory category) {
        List<Driver> activeDrivers = driverRepository.findActiveOnlineDrivers(Driver.VerificationStatus.APPROVED);

        return activeDrivers.stream()
                .filter(driver -> {
                    if (driver.getCurrentLatitude() == null || driver.getCurrentLongitude() == null) {
                        return false;
                    }
                    double dist = calculateHaversineDistanceKm(latitude, longitude, driver.getCurrentLatitude(), driver.getCurrentLongitude());
                    return dist <= DEFAULT_SEARCH_RADIUS_KM;
                })
                .filter(driver -> {
                    Optional<Vehicle> vehicleOpt = vehicleRepository.findByDriverId(driver.getId());
                    return vehicleOpt.isPresent() && 
                           vehicleOpt.get().getCategory() == category &&
                           vehicleOpt.get().getVerificationStatus() == Vehicle.VerificationStatus.APPROVED;
                })
                .collect(Collectors.toList());
    }

    /**
     * Haversine formula to compute great-circle distance between two GPS coordinates in Kilometers
     */
    public static double calculateHaversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in KM
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}

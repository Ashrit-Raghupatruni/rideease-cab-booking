package com.rideease.repository;

import com.rideease.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    Optional<Vehicle> findByDriverId(String driverId);
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);
    List<Vehicle> findByVerificationStatus(Vehicle.VerificationStatus status);
}

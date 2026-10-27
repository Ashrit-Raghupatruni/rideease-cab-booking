package com.rideease.repository;

import com.rideease.model.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    Optional<Vehicle> findByDriverId(String driverId);
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);
}

package com.rideease.repository;

import com.rideease.model.Driver;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends MongoRepository<Driver, String> {
    Optional<Driver> findByUserId(String userId);

    // Geospatial query to find verified, available, online drivers within radius
    List<Driver> findByVerificationStatusAndOnlineTrueAndAvailableTrueAndCurrentLocationNear(
            Driver.VerificationStatus status,
            Point point,
            Distance distance
    );

    List<Driver> findByVerificationStatus(Driver.VerificationStatus status);
}

package com.rideease.repository;

import com.rideease.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {
    Optional<Driver> findByUserId(String userId);

    List<Driver> findByVerificationStatus(Driver.VerificationStatus status);

    List<Driver> findByVerificationStatusAndOnlineTrueAndAvailableTrue(Driver.VerificationStatus status);

    /**
     * Finds nearby available verified drivers using SQL Haversine spherical distance formula
     * 6371 * acos(cos(radians(:lat)) * cos(radians(currentLatitude)) * cos(radians(currentLongitude) - radians(:lon)) + sin(radians(:lat)) * sin(radians(currentLatitude)))
     */
    @Query(value = "SELECT d FROM Driver d WHERE d.verificationStatus = :status AND d.online = true AND d.available = true " +
                   "AND d.currentLatitude IS NOT NULL AND d.currentLongitude IS NOT NULL")
    List<Driver> findActiveOnlineDrivers(@Param("status") Driver.VerificationStatus status);
}

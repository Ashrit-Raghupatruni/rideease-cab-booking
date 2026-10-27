package com.rideease.repository;

import com.rideease.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByPassengerIdOrderByCreatedAtDesc(String passengerId);
    List<Booking> findByDriverIdOrderByCreatedAtDesc(String driverId);
    List<Booking> findByStatus(Booking.BookingStatus status);
}

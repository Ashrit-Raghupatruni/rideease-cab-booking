package com.rideease.repository;

import com.rideease.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByBookingId(String bookingId);
    List<Review> findByDriverId(String driverId);
    List<Review> findByPassengerId(String passengerId);
}

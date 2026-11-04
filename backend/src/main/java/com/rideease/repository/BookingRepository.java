package com.rideease.repository;

import com.rideease.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    List<Booking> findByPassengerIdOrderByCreatedAtDesc(String passengerId);
    List<Booking> findByDriverIdOrderByCreatedAtDesc(String driverId);
    List<Booking> findByStatus(Booking.BookingStatus status);
    List<Booking> findByStatusIn(List<Booking.BookingStatus> statuses);
}

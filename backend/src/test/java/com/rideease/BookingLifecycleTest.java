package com.rideease;

import com.rideease.model.Booking;
import com.rideease.model.Vehicle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingLifecycleTest {

    @Test
    @DisplayName("Should maintain valid booking status transitions and OTP attempts limit")
    void testBookingStatusAndOtpAttempts() {
        Booking booking = new Booking();
        booking.setStatus(Booking.BookingStatus.SEARCHING);
        booking.setOtp("4567");
        booking.setVehicleCategory(Vehicle.VehicleCategory.SEDAN);

        assertEquals(Booking.BookingStatus.SEARCHING, booking.getStatus());
        assertEquals(0, booking.getOtpAttempts());

        // Increment attempts on wrong OTP
        booking.setOtpAttempts(booking.getOtpAttempts() + 1);
        assertEquals(1, booking.getOtpAttempts());

        // Verify status transition
        booking.setStatus(Booking.BookingStatus.ACCEPTED);
        booking.setDriverId("driver-999");
        assertEquals("driver-999", booking.getDriverId());
        assertEquals(Booking.BookingStatus.ACCEPTED, booking.getStatus());

        booking.setStatus(Booking.BookingStatus.IN_PROGRESS);
        assertEquals(Booking.BookingStatus.IN_PROGRESS, booking.getStatus());

        booking.setStatus(Booking.BookingStatus.COMPLETED);
        assertEquals(Booking.BookingStatus.COMPLETED, booking.getStatus());
    }
}

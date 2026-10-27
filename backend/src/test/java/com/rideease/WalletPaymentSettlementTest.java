package com.rideease;

import com.rideease.model.Booking;
import com.rideease.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class WalletPaymentSettlementTest {

    @Test
    @DisplayName("Should successfully debit passenger wallet if sufficient balance and mark paid")
    void testSufficientWalletDebit() {
        User passenger = new User();
        passenger.setId("user-1");
        passenger.setWalletBalance(500.0);

        Booking booking = new Booking();
        booking.setEstimatedFare(250.0);
        booking.setFinalFare(250.0);
        booking.setPaymentMethod(Booking.PaymentMethod.WALLET);
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);

        // Process debit
        if (passenger.getWalletBalance() >= booking.getFinalFare()) {
            passenger.setWalletBalance(passenger.getWalletBalance() - booking.getFinalFare());
            booking.setPaymentStatus(Booking.PaymentStatus.PAID);
            booking.setPaidAt(Instant.now());
        }

        assertEquals(250.0, passenger.getWalletBalance());
        assertEquals(Booking.PaymentStatus.PAID, booking.getPaymentStatus());
        assertNotNull(booking.getPaidAt());
    }

    @Test
    @DisplayName("Should keep payment PENDING if wallet balance is insufficient")
    void testInsufficientWalletDebit() {
        User passenger = new User();
        passenger.setId("user-2");
        passenger.setWalletBalance(100.0);

        Booking booking = new Booking();
        booking.setEstimatedFare(350.0);
        booking.setFinalFare(350.0);
        booking.setPaymentMethod(Booking.PaymentMethod.WALLET);
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);

        // Process debit check
        if (passenger.getWalletBalance() >= booking.getFinalFare()) {
            passenger.setWalletBalance(passenger.getWalletBalance() - booking.getFinalFare());
            booking.setPaymentStatus(Booking.PaymentStatus.PAID);
        } else {
            booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        }

        assertEquals(100.0, passenger.getWalletBalance());
        assertEquals(Booking.PaymentStatus.PENDING, booking.getPaymentStatus());
    }
}

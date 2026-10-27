package com.rideease;

import com.rideease.dto.BookRideRequest;
import com.rideease.model.Booking;
import com.rideease.model.Vehicle;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RideBookingValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid BookRideRequest should have zero constraint violations")
    void testValidBookRideRequest() {
        BookRideRequest req = new BookRideRequest();
        req.setPickupAddress("SRM University AP");
        req.setDropoffAddress("Vijayawada Railway Station");
        req.setPickupLat(16.4445);
        req.setPickupLon(80.5085);
        req.setDropoffLat(16.5175);
        req.setDropoffLon(80.6200);
        req.setVehicleCategory(Vehicle.VehicleCategory.SEDAN);
        req.setPaymentMethod(Booking.PaymentMethod.CASH);

        Set<ConstraintViolation<BookRideRequest>> violations = validator.validate(req);
        assertTrue(violations.isEmpty(), "Expected zero violations for valid booking request");
    }

    @Test
    @DisplayName("Should fail validation on invalid latitude/longitude out of bounds")
    void testOutOfBoundsCoordinates() {
        BookRideRequest req = new BookRideRequest();
        req.setPickupAddress("Invalid Location");
        req.setDropoffAddress("Valid Dropoff");
        req.setPickupLat(95.5); // Invalid: latitude > 90.0
        req.setPickupLon(190.0); // Invalid: longitude > 180.0
        req.setDropoffLat(16.5175);
        req.setDropoffLon(80.6200);
        req.setVehicleCategory(Vehicle.VehicleCategory.SEDAN);
        req.setPaymentMethod(Booking.PaymentMethod.CASH);

        Set<ConstraintViolation<BookRideRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty(), "Expected violations for invalid coordinates");
        assertEquals(2, violations.size());
    }

    @Test
    @DisplayName("Should fail validation on blank pickup or dropoff address")
    void testBlankAddress() {
        BookRideRequest req = new BookRideRequest();
        req.setPickupAddress("   ");
        req.setDropoffAddress("");
        req.setPickupLat(16.4445);
        req.setPickupLon(80.5085);
        req.setDropoffLat(16.5175);
        req.setDropoffLon(80.6200);
        req.setVehicleCategory(Vehicle.VehicleCategory.SEDAN);
        req.setPaymentMethod(Booking.PaymentMethod.CASH);

        Set<ConstraintViolation<BookRideRequest>> violations = validator.validate(req);
        assertEquals(2, violations.size());
    }
}

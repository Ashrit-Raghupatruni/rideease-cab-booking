package com.rideease;

import com.rideease.model.Vehicle;
import com.rideease.service.FareCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class FareCalculationServiceTest {

    private FareCalculationService fareService;

    @BeforeEach
    void setUp() {
        fareService = new FareCalculationService();
        ReflectionTestUtils.setField(fareService, "baseMini", 30.0);
        ReflectionTestUtils.setField(fareService, "baseSedan", 50.0);
        ReflectionTestUtils.setField(fareService, "baseSuv", 80.0);
        ReflectionTestUtils.setField(fareService, "perKmMini", 10.0);
        ReflectionTestUtils.setField(fareService, "perKmSedan", 14.0);
        ReflectionTestUtils.setField(fareService, "perKmSuv", 18.0);
        ReflectionTestUtils.setField(fareService, "perMinute", 2.0);
    }

    @Test
    @DisplayName("Should correctly calculate fare for Sedan tier")
    void testSedanFare() {
        // Base: 50.0, Distance: 10.0km * 14.0 = 140.0, Duration: 20 mins * 2.0 = 40.0 -> Total: 230.0
        double fare = fareService.calculateFare(Vehicle.VehicleCategory.SEDAN, 10.0, 20, 1.0);
        assertEquals(230.0, fare, 0.01);
    }

    @Test
    @DisplayName("Should correctly calculate fare for Mini tier")
    void testMiniFare() {
        // Base: 30.0, Distance: 10.0km * 10.0 = 100.0, Duration: 20 mins * 2.0 = 40.0 -> Total: 170.0
        double fare = fareService.calculateFare(Vehicle.VehicleCategory.MINI, 10.0, 20, 1.0);
        assertEquals(170.0, fare, 0.01);
    }

    @Test
    @DisplayName("Should correctly calculate fare for SUV tier")
    void testSuvFare() {
        // Base: 80.0, Distance: 10.0km * 18.0 = 180.0, Duration: 20 mins * 2.0 = 40.0 -> Total: 300.0
        double fare = fareService.calculateFare(Vehicle.VehicleCategory.SUV, 10.0, 20, 1.0);
        assertEquals(300.0, fare, 0.01);
    }

    @Test
    @DisplayName("Should apply surge multiplier correctly")
    void testSurgePricing() {
        double standardFare = fareService.calculateFare(Vehicle.VehicleCategory.SEDAN, 10.0, 20, 1.0);
        double surgeFare = fareService.calculateFare(Vehicle.VehicleCategory.SEDAN, 10.0, 20, 1.5);
        assertEquals(standardFare * 1.5, surgeFare, 0.01);
    }
}

package com.rideease.service;

import com.rideease.model.Vehicle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FareCalculationService {

    @Value("${fare.base.mini:30.0}")
    private double baseMini;

    @Value("${fare.base.sedan:50.0}")
    private double baseSedan;

    @Value("${fare.base.suv:80.0}")
    private double baseSuv;

    @Value("${fare.per-km.mini:10.0}")
    private double perKmMini;

    @Value("${fare.per-km.sedan:14.0}")
    private double perKmSedan;

    @Value("${fare.per-km.suv:18.0}")
    private double perKmSuv;

    @Value("${fare.per-min:2.0}")
    private double perMinute;

    /**
     * Calculates the estimated or final fare based on formula:
     * Fare = Base + (Distance * Rate/km) + (Duration * Rate/min)
     */
    public double calculateFare(Vehicle.VehicleCategory category, double distanceKm, int durationMinutes, double surgeMultiplier) {
        double baseRate;
        double perKmRate;

        switch (category != null ? category : Vehicle.VehicleCategory.SEDAN) {
            case MINI -> {
                baseRate = baseMini;
                perKmRate = perKmMini;
            }
            case SUV -> {
                baseRate = baseSuv;
                perKmRate = perKmSuv;
            }
            case SEDAN -> {
                baseRate = baseSedan;
                perKmRate = perKmSedan;
            }
            default -> {
                baseRate = baseSedan;
                perKmRate = perKmSedan;
            }
        }

        double surge = surgeMultiplier > 0 ? surgeMultiplier : 1.0;
        double rawFare = (baseRate + (distanceKm * perKmRate) + (durationMinutes * perMinute)) * surge;

        // Round to 2 decimal places
        return BigDecimal.valueOf(rawFare)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}

package com.rideease;

import com.rideease.service.RoadRoutingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoadRoutingServiceTest {

    private RoadRoutingService routingService;

    @BeforeEach
    void setUp() {
        routingService = new RoadRoutingService();
    }

    @Test
    @DisplayName("Should reject impossible out-of-bounds coordinates without attempting HTTP call")
    void testOutOfBoundsCoordinatesRejected() {
        RoadRoutingService.RoutingResult result = routingService.calculateRoadRoute(
                120.0, 80.0, 16.5, 80.6
        );

        assertFalse(result.isSuccess());
        assertEquals("InvalidQuery", result.getCode());
        assertEquals(0.0, result.getDistanceKm());
    }

    @Test
    @DisplayName("Snapping check returns isRoutable false when coordinates are invalid or disconnected")
    void testSnappingFailureHandling() {
        RoadRoutingService.SnappingResult snap = routingService.checkNearestRoad(999.0, 999.0);
        assertFalse(snap.isRoutable(), "Invalid coordinates must not be reported as routable");
    }
}

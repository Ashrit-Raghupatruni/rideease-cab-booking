package com.rideease.controller;

import com.rideease.service.MapplsRoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MapplsApiController {

    @Autowired
    private MapplsRoutingService mapplsRoutingService;

    @GetMapping("/api/maps/route")
    public Map<String, Object> getRoute(
            @RequestParam(defaultValue = "77.5801") double pickupLon,
            @RequestParam(defaultValue = "12.9720") double pickupLat,
            @RequestParam(defaultValue = "77.6050") double dropoffLon,
            @RequestParam(defaultValue = "12.9810") double dropoffLat
    ) {
        return mapplsRoutingService.calculateRoute(pickupLon, pickupLat, dropoffLon, dropoffLat);
    }
}

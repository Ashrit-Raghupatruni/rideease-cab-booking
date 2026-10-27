package com.rideease.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Authoritative Road Routing & Verification Engine.
 * Strictly calculates driving geometry, distance, and duration using actual road networks.
 * Never fabricates or synthesizes coordinates or distance.
 */
@Service
public class RoadRoutingService {

    private static final Logger log = LoggerFactory.getLogger(RoadRoutingService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Standard routing mirrors for high availability
    private static final String[] ROUTING_MIRRORS = new String[]{
            "https://router.project-osrm.org",
            "https://routing.openstreetmap.de/routed-car"
    };

    public RoadRoutingService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(4000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * Queries authoritative road routing between pickup and dropoff coordinates.
     * Coordinate order for OSRM URL: {lon},{lat};{lon},{lat}
     */
    public RoutingResult calculateRoadRoute(double pLat, double pLon, double dLat, double dLon) {
        // Validate coordinates
        if (pLat < -90.0 || pLat > 90.0 || dLat < -90.0 || dLat > 90.0 ||
            pLon < -180.0 || pLon > 180.0 || dLon < -180.0 || dLon > 180.0) {
            return RoutingResult.failed("InvalidQuery", "Coordinates out of valid geographical bounds");
        }

        String lastError = "Could not contact routing service";
        String lastCode = "ConnectionError";

        for (String baseUrl : ROUTING_MIRRORS) {
            try {
                String osrmUrl = String.format(Locale.US,
                        "%s/route/v1/driving/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson",
                        baseUrl, pLon, pLat, dLon, dLat);

                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "RideEase-CabBooking/1.0");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(osrmUrl, HttpMethod.GET, entity, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    String code = root.path("code").asText("");

                    if ("Ok".equalsIgnoreCase(code)) {
                        JsonNode bestRoute = root.path("routes").get(0);
                        double distMeters = bestRoute.path("distance").asDouble();
                        double durSeconds = bestRoute.path("duration").asDouble();
                        JsonNode geometryNode = bestRoute.path("geometry");

                        double distKm = Math.round((distMeters / 1000.0) * 10.0) / 10.0;
                        if (distKm < 0.1) distKm = 0.1;
                        int durMins = (int) Math.round(durSeconds / 60.0);
                        if (durMins < 1) durMins = 1;

                        Map<String, Object> geom = objectMapper.convertValue(geometryNode, Map.class);
                        return RoutingResult.success(distKm, durMins, geom, baseUrl);
                    } else {
                        lastCode = code;
                        lastError = root.path("message").asText("No road route found between selected points.");
                    }
                }
            } catch (Exception e) {
                lastError = e.getMessage();
                log.warn("Routing mirror ({}) failed: {}", baseUrl, e.getMessage());
            }
        }

        return RoutingResult.failed(lastCode, lastError);
    }

    /**
     * Checks nearest drivable road to snap coordinate.
     * Does NOT return routable=true if network fails.
     */
    public SnappingResult checkNearestRoad(double lat, double lon) {
        for (String baseUrl : ROUTING_MIRRORS) {
            try {
                String nearestUrl = String.format(Locale.US,
                        "%s/nearest/v1/driving/%.6f,%.6f", baseUrl, lon, lat);

                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Agent", "RideEase-CabBooking/1.0");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(nearestUrl, HttpMethod.GET, entity, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    if ("Ok".equalsIgnoreCase(root.path("code").asText(""))) {
                        JsonNode wp = root.path("waypoints").get(0);
                        double distance = wp.path("distance").asDouble();
                        JsonNode loc = wp.path("location");
                        double snapLon = loc.get(0).asDouble();
                        double snapLat = loc.get(1).asDouble();
                        String roadName = wp.path("name").asText("Accessible Road");

                        boolean isRoutable = distance < 300.0;
                        return new SnappingResult(true, "Ok", distance, snapLat, snapLon, roadName, isRoutable, "Road verified");
                    }
                }
            } catch (Exception ignored) {
                // Try next mirror
            }
        }

        // Must report isRoutable as false when verification fails
        return new SnappingResult(false, "ConnectionFailure", -1.0, lat, lon, "", false, "Could not verify road accessibility");
    }

    public static class RoutingResult {
        private final boolean success;
        private final String code;
        private final String message;
        private final double distanceKm;
        private final int durationMinutes;
        private final Map<String, Object> geometry;
        private final String provider;

        private RoutingResult(boolean success, String code, String message, double distanceKm, int durationMinutes, Map<String, Object> geometry, String provider) {
            this.success = success;
            this.code = code;
            this.message = message;
            this.distanceKm = distanceKm;
            this.durationMinutes = durationMinutes;
            this.geometry = geometry;
            this.provider = provider;
        }

        public static RoutingResult success(double distKm, int durMins, Map<String, Object> geom, String provider) {
            return new RoutingResult(true, "Ok", null, distKm, durMins, geom, provider);
        }

        public static RoutingResult failed(String code, String message) {
            return new RoutingResult(false, code, message, 0.0, 0, null, null);
        }

        public boolean isSuccess() { return success; }
        public String getCode() { return code; }
        public String getMessage() { return message; }
        public double getDistanceKm() { return distanceKm; }
        public int getDurationMinutes() { return durationMinutes; }
        public Map<String, Object> getGeometry() { return geometry; }
        public String getProvider() { return provider; }
    }

    public static class SnappingResult {
        private final boolean verified;
        private final String code;
        private final double distanceMeters;
        private final double snappedLat;
        private final double snappedLon;
        private final String roadName;
        private final boolean isRoutable;
        private final String message;

        public SnappingResult(boolean verified, String code, double distanceMeters, double snappedLat, double snappedLon, String roadName, boolean isRoutable, String message) {
            this.verified = verified;
            this.code = code;
            this.distanceMeters = distanceMeters;
            this.snappedLat = snappedLat;
            this.snappedLon = snappedLon;
            this.roadName = roadName;
            this.isRoutable = isRoutable;
            this.message = message;
        }

        public boolean isVerified() { return verified; }
        public String getCode() { return code; }
        public double getDistanceMeters() { return distanceMeters; }
        public double getSnappedLat() { return snappedLat; }
        public double getSnappedLon() { return snappedLon; }
        public String getRoadName() { return roadName; }
        public boolean isRoutable() { return isRoutable; }
        public String getMessage() { return message; }
    }
}

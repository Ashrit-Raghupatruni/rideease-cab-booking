package com.rideease.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
public class GeoRoutingController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${mappls.api-key:nofirpxgqwhpegaxdnvwcotwrbptisymmmnw}")
    private String mapplsApiKey;

    /**
     * Address Autocomplete Suggestions
     */
    @GetMapping("/api/geo/autocomplete")
    public List<Map<String, Object>> autocomplete(@RequestParam("q") String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        if (query == null || query.trim().length() < 2) {
            return results;
        }

        try {
            String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            String url = "https://photon.komoot.io/api/?q=" + encoded + "&limit=6&lat=16.5&lon=80.6";

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "RideEase-CabBooking/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode features = root.path("features");
                for (JsonNode f : features) {
                    JsonNode props = f.path("properties");
                    JsonNode geom = f.path("geometry").path("coordinates");

                    double lon = geom.get(0).asDouble();
                    double lat = geom.get(1).asDouble();

                    String name = props.path("name").asText("");
                    String city = props.path("city").asText(props.path("county").asText(""));
                    String state = props.path("state").asText("");
                    String country = props.path("country").asText("");

                    StringBuilder label = new StringBuilder(name);
                    if (!city.isEmpty() && !name.contains(city)) label.append(", ").append(city);
                    if (!state.isEmpty()) label.append(", ").append(state);
                    if (!country.isEmpty()) label.append(", ").append(country);

                    Map<String, Object> item = new HashMap<>();
                    item.put("display_name", label.toString());
                    item.put("lat", lat);
                    item.put("lon", lon);
                    results.add(item);
                }
            }
        } catch (Exception e) {
            // Fallback mock items if internet request has temporary DNS latency
            System.err.println("Autocomplete notice: " + e.getMessage());
        }

        return results;
    }

    /**
     * Reverse Geocoding (Coordinates -> Address)
     */
    @GetMapping("/api/geo/reverse")
    public Map<String, Object> reverseGeocode(@RequestParam("lat") double lat, @RequestParam("lon") double lon) {
        Map<String, Object> result = new HashMap<>();
        result.put("lat", lat);
        result.put("lon", lon);

        try {
            String url = "https://photon.komoot.io/reverse?lat=" + lat + "&lon=" + lon;
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "RideEase-CabBooking/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode features = root.path("features");
                if (features.isArray() && features.size() > 0) {
                    JsonNode props = features.get(0).path("properties");
                    String name = props.path("name").asText(props.path("street").asText("Selected Location"));
                    String city = props.path("city").asText(props.path("county").asText(""));
                    String state = props.path("state").asText("");

                    StringBuilder label = new StringBuilder(name);
                    if (!city.isEmpty()) label.append(", ").append(city);
                    if (!state.isEmpty()) label.append(", ").append(state);

                    result.put("address", label.toString());
                    return result;
                }
            }
        } catch (Exception ignored) {}

        result.put("address", String.format(Locale.US, "Location (%.4f, %.4f)", lat, lon));
        return result;
    }

    @GetMapping("/api/geo/nearest")
    public Map<String, Object> checkNearestRoad(@RequestParam("lat") double lat, @RequestParam("lon") double lon) {
        Map<String, Object> result = new HashMap<>();
        String[] mirrors = new String[]{
                "https://router.project-osrm.org",
                "https://routing.openstreetmap.de/routed-car"
        };

        for (String baseUrl : mirrors) {
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

                        result.put("status", "SUCCESS");
                        result.put("code", "Ok");
                        result.put("distanceMeters", distance);
                        result.put("snappedLat", snapLat);
                        result.put("snappedLon", snapLon);
                        result.put("roadName", roadName);
                        // If within 300 meters, considered directly routable
                        result.put("isRoutable", distance < 300.0);
                        return result;
                    }
                }
            } catch (Exception e) {
                // Try next mirror
            }
        }

        result.put("status", "UNVERIFIED");
        result.put("code", "ConnectionFailure");
        result.put("message", "Could not query nearest road network");
        result.put("isRoutable", true);
        return result;
    }

    /**
     * Resilient Road Routing Engine
     * 1. Direct OSRM router with secondary routing mirror failover
     * 2. Inspects and returns exact status (Ok, NoRoute, NoSegment, InvalidQuery, NetworkFailure)
     * 3. Zero mock data: Returns authentic distance, duration, and GeoJSON geometry
     */
    @GetMapping("/api/geo/route")
    public Map<String, Object> calculateRoadRoute(
            @RequestParam("pLat") double pLat,
            @RequestParam("pLon") double pLon,
            @RequestParam("dLat") double dLat,
            @RequestParam("dLon") double dLon
    ) {
        Map<String, Object> result = new HashMap<>();

        String[] mirrors = new String[]{
                "https://router.project-osrm.org",
                "https://routing.openstreetmap.de/routed-car"
        };

        String lastError = "Could not reach routing servers";
        String lastCode = "NetworkFailure";

        for (String baseUrl : mirrors) {
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

                        result.put("status", "SUCCESS");
                        result.put("code", "Ok");
                        result.put("distanceKm", distKm);
                        result.put("durationMinutes", durMins);
                        result.put("geometry", objectMapper.convertValue(geometryNode, Map.class));
                        result.put("routingProvider", "OSRM Road Router (" + baseUrl + ")");
                        return result;
                    } else {
                        lastCode = code;
                        lastError = root.path("message").asText("No valid drivable road route found between selected points.");
                    }
                }
            } catch (Exception e) {
                lastError = e.getMessage();
                System.err.println("Routing mirror (" + baseUrl + ") error: " + e.getMessage());
            }
        }

        // Return authentic failure details (No synthetic / fake routes!)
        result.put("status", "FAILED");
        result.put("code", lastCode);
        result.put("message", lastError);
        return result;
    }
}

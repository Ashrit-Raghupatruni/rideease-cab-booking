package com.rideease.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideease.service.RoadRoutingService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private RoadRoutingService roadRoutingService;

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
        } catch (Exception ignored) {}

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

    /**
     * Snaps a point to nearest drivable road and verifies accessibility.
     */
    @GetMapping("/api/geo/nearest")
    public Map<String, Object> checkNearestRoad(@RequestParam("lat") double lat, @RequestParam("lon") double lon) {
        RoadRoutingService.SnappingResult snap = roadRoutingService.checkNearestRoad(lat, lon);
        Map<String, Object> result = new HashMap<>();
        result.put("status", snap.isVerified() ? "SUCCESS" : "FAILED");
        result.put("code", snap.getCode());
        result.put("distanceMeters", snap.getDistanceMeters());
        result.put("snappedLat", snap.getSnappedLat());
        result.put("snappedLon", snap.getSnappedLon());
        result.put("roadName", snap.getRoadName());
        result.put("isRoutable", snap.isRoutable());
        result.put("message", snap.getMessage());
        return result;
    }

    /**
     * Authoritative Road Routing Endpoint
     */
    @GetMapping("/api/geo/route")
    public Map<String, Object> calculateRoadRoute(
            @RequestParam("pLat") double pLat,
            @RequestParam("pLon") double pLon,
            @RequestParam("dLat") double dLat,
            @RequestParam("dLon") double dLon
    ) {
        RoadRoutingService.RoutingResult routing = roadRoutingService.calculateRoadRoute(pLat, pLon, dLat, dLon);
        Map<String, Object> result = new HashMap<>();

        if (routing.isSuccess()) {
            result.put("status", "SUCCESS");
            result.put("code", "Ok");
            result.put("distanceKm", routing.getDistanceKm());
            result.put("durationMinutes", routing.getDurationMinutes());
            result.put("geometry", routing.getGeometry());
            result.put("routingProvider", routing.getProvider());
        } else {
            result.put("status", "FAILED");
            result.put("code", routing.getCode());
            result.put("message", routing.getMessage());
        }

        return result;
    }
}

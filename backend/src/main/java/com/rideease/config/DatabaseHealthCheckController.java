package com.rideease.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class DatabaseHealthCheckController {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthCheckController.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @GetMapping("/api/health/db")
    public Map<String, Object> testDatabaseReadWrite() {
        Map<String, Object> response = new HashMap<>();
        String testId = "health-test-" + UUID.randomUUID();

        try (MongoClient client = MongoClients.create(mongoUri)) {
            MongoDatabase db = client.getDatabase("rideease_db");
            MongoCollection<Document> collection = db.getCollection("connectivity_test");

            // 1. TEST WRITE
            long writeStart = System.currentTimeMillis();
            Document testDoc = new Document("_id", testId)
                    .append("description", "MongoDB Health Check")
                    .append("timestamp", Instant.now().toString())
                    .append("status", "ACTIVE");
            collection.insertOne(testDoc);
            long writeLatency = System.currentTimeMillis() - writeStart;

            // 2. TEST READ
            long readStart = System.currentTimeMillis();
            Document retrievedDoc = collection.find(new Document("_id", testId)).first();
            long readLatency = System.currentTimeMillis() - readStart;

            // 3. CLEANUP
            collection.deleteOne(new Document("_id", testId));

            if (retrievedDoc != null) {
                response.put("status", "UP");
                response.put("writeLatencyMs", writeLatency);
                response.put("readLatencyMs", readLatency);
                response.put("message", "Database read/write check passed");
            } else {
                response.put("status", "ERROR");
                response.put("message", "Document was written but could not be read back.");
            }

        } catch (Exception e) {
            log.error("Database health check failed", e);
            response.put("status", "DOWN");
            response.put("message", "Database connection check failed");
        }

        return response;
    }
}

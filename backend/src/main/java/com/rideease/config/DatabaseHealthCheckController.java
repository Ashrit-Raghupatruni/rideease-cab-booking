package com.rideease.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class DatabaseHealthCheckController {

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
                    .append("description", "MongoDB Atlas Live Read/Write Health Check")
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
                response.put("database", "MongoDB Atlas Cluster0");
                response.put("databaseName", "rideease_db");
                response.put("writeTest", "SUCCESS (" + writeLatency + " ms)");
                response.put("readTest", "SUCCESS (" + readLatency + " ms)");
                response.put("insertedDocumentId", testId);
                response.put("retrievedDocument", retrievedDoc.toJson());
                response.put("message", "Database is 100% OPERATIONAL for both READ and WRITE operations!");
            } else {
                response.put("status", "ERROR");
                response.put("message", "Document was written but could not be read back.");
            }

        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("error", e.getClass().getSimpleName());
            response.put("errorMessage", e.getMessage());
        }

        return response;
    }
}

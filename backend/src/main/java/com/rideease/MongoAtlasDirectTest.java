package com.rideease;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.Instant;
import java.util.UUID;

/**
 * Direct standalone MongoDB Atlas Read/Write Test
 */
public class MongoAtlasDirectTest {

    public static void main(String[] args) {
        String uri = "mongodb+srv://yeswanthpillimitla_db_user:GpLoKeBhznWnGRsS@cluster0.5nggeea.mongodb.net/rideease_db?retryWrites=true&w=majority&appName=Cluster0&tls=true";

        System.out.println("=== Starting MongoDB Atlas Read/Write Verification ===");
        System.out.println("Connecting to Cluster0...");

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("rideease_db");
            MongoCollection<Document> testCollection = database.getCollection("connectivity_test");

            String testId = UUID.randomUUID().toString();
            Instant now = Instant.now();

            // 1. WRITE TEST
            System.out.println("\n[1/3] Testing Document INSERT (Write)...");
            Document doc = new Document("_id", testId)
                    .append("testName", "RideEase Atlas ReadWrite Verification")
                    .append("status", "SUCCESS")
                    .append("timestamp", now.toString());

            testCollection.insertOne(doc);
            System.out.println(">>> WRITE SUCCESS! Inserted Document with ID: " + testId);

            // 2. READ TEST
            System.out.println("\n[2/3] Testing Document FIND (Read)...");
            Document foundDoc = testCollection.find(new Document("_id", testId)).first();

            if (foundDoc != null) {
                System.out.println(">>> READ SUCCESS! Retrieved Document: " + foundDoc.toJson());
            } else {
                System.err.println(">>> READ FAILED: Document not found!");
                System.exit(1);
            }

            // 3. CLEANUP (DELETE)
            System.out.println("\n[3/3] Cleaning up test document...");
            testCollection.deleteOne(new Document("_id", testId));
            System.out.println(">>> CLEANUP SUCCESS! Deleted temporary test document.");

            System.out.println("\n=======================================================");
            System.out.println("VERIFICATION PASSED: Database READ and WRITE are 100% OPERATIONAL!");
            System.out.println("=======================================================");

        } catch (Exception e) {
            System.err.println("\n>>> MONGODB TEST ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

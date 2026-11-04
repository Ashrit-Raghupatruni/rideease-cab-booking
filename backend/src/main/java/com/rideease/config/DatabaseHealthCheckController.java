package com.rideease.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DatabaseHealthCheckController {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthCheckController.class);

    @Autowired
    private DataSource dataSource;

    @GetMapping("/api/health/db")
    public Map<String, Object> testDatabaseReadWrite() {
        Map<String, Object> response = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String dbProductName = metaData.getDatabaseProductName();
            String dbProductVersion = metaData.getDatabaseProductVersion();

            // 1. TEST WRITE / DDL or temp table query
            long writeStart = System.currentTimeMillis();
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS connectivity_test (id VARCHAR(50) PRIMARY KEY, ts VARCHAR(50))");
                statement.executeUpdate("INSERT INTO connectivity_test (id, ts) VALUES ('health-" + System.currentTimeMillis() + "', '" + Instant.now() + "')");
            }
            long writeLatency = System.currentTimeMillis() - writeStart;

            // 2. TEST READ
            long readStart = System.currentTimeMillis();
            int count = 0;
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT count(*) FROM connectivity_test")) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
            long readLatency = System.currentTimeMillis() - readStart;

            response.put("status", "HEALTHY");
            response.put("database", dbProductName + " " + dbProductVersion);
            response.put("readLatencyMs", readLatency);
            response.put("writeLatencyMs", writeLatency);
            response.put("testRecordsCount", count);
            return response;

        } catch (Exception e) {
            log.error("Database health check failed: {}", e.getMessage(), e);
            response.put("status", "UNHEALTHY");
            response.put("error", e.getMessage());
            return response;
        }
    }
}

package com.rideease;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class RideEaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(RideEaseApplication.class, args);
    }
}

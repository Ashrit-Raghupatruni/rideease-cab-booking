package com.rideease;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.io.File;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableMongoAuditing
public class RideEaseApplication {

    public static void main(String[] args) {
        // Robustly load local .env file into System properties if present before Spring initializes
        try {
            Dotenv dotenv = null;
            if (new File(".env").exists()) {
                dotenv = Dotenv.configure().ignoreIfMissing().load();
            } else if (new File("backend/.env").exists()) {
                dotenv = Dotenv.configure().directory("backend").ignoreIfMissing().load();
            } else if (new File("../backend/.env").exists()) {
                dotenv = Dotenv.configure().directory("../backend").ignoreIfMissing().load();
            } else {
                dotenv = Dotenv.configure().ignoreIfMissing().load();
            }

            if (dotenv != null) {
                dotenv.entries().forEach(entry -> {
                    if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                        System.setProperty(entry.getKey(), entry.getValue());
                    }
                });
            }
        } catch (Exception ignored) {}

        SpringApplication.run(RideEaseApplication.class, args);
    }
}

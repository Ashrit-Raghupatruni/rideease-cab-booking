package com.rideease.config;

import com.cloudinary.Cloudinary;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.url:}")
    private String configuredCloudinaryUrl;

    @Bean
    public Cloudinary cloudinary() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String cldUrl = dotenv.get("CLOUDINARY_URL");

        if (cldUrl == null || cldUrl.isBlank()) {
            cldUrl = configuredCloudinaryUrl;
        }
        if (cldUrl == null || cldUrl.isBlank()) {
            cldUrl = System.getenv("CLOUDINARY_URL");
        }

        if (cldUrl != null && !cldUrl.isBlank()) {
            return new Cloudinary(cldUrl);
        }

        // Return unconfigured instance rather than embedding hard-coded account secrets
        return new Cloudinary();
    }
}

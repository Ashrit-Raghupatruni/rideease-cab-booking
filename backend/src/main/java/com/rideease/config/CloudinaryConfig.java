package com.rideease.config;

import com.cloudinary.Cloudinary;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        // Load environment variables from .env file or system environment
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        String cloudinaryUrl = dotenv.get("CLOUDINARY_URL");
        if (cloudinaryUrl == null || cloudinaryUrl.isBlank()) {
            cloudinaryUrl = System.getenv("CLOUDINARY_URL");
        }

        if (cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
            return new Cloudinary(cloudinaryUrl);
        }

        // Fallback default
        return new Cloudinary("cloudinary://795713391115231:mCCJIMWYknyBu47nNFKQYsvy7kI@sy4opzcj");
    }
}

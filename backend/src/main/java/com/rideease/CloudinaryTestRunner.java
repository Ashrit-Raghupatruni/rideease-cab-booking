package com.rideease;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Map;

/**
 * Cloudinary Quickstart Test & Verification
 */
public class CloudinaryTestRunner {

    public static void main(String[] args) {
        try {
            System.out.println("--- Starting Cloudinary Verification ---");

            // 1. Load credentials using Dotenv
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            String cldUrl = dotenv.get("CLOUDINARY_URL");
            if (cldUrl == null) {
                cldUrl = "cloudinary://795713391115231:mCCJIMWYknyBu47nNFKQYsvy7kI@sy4opzcj";
            }

            Cloudinary cloudinary = new Cloudinary(cldUrl);
            System.out.println("Connected to Cloudinary Cloud Name: " + cloudinary.config.cloudName);

            // 2. Upload test image
            Map<?, ?> params1 = ObjectUtils.asMap(
                    "use_filename", true,
                    "unique_filename", false,
                    "overwrite", true,
                    "folder", "rideease/test"
            );

            System.out.println("Uploading sample asset...");
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    "https://cloudinary-devs.github.io/cld-docs-assets/assets/images/coffee_cup.jpg",
                    params1
            );
            System.out.println("Upload successful! Public URL: " + uploadResult.get("secure_url"));

            // 3. Transformation demo
            String transformedUrl = cloudinary.url().transformation(new Transformation()
                    .crop("pad")
                    .width(300)
                    .height(400)
                    .background("auto:predominant"))
                    .generate("rideease/test/coffee_cup");

            System.out.println("Transformed Image URL: " + transformedUrl);
            System.out.println("--- Cloudinary Integration Verified Successfully! ---");

        } catch (Exception e) {
            System.err.println("Cloudinary error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

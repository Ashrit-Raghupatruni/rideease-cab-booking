package com.rideease.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class MediaStorageService {

    private static final Logger log = LoggerFactory.getLogger(MediaStorageService.class);

    @Autowired
    private Cloudinary cloudinary;

    /**
     * Upload an image or verification document to Cloudinary under the specified folder.
     * Returns the secure HTTPS URL from Cloudinary.
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "rideease/" + folder,
                    "resource_type", "auto" // Supports images and PDF documents (RC, Insurance, License)
            ));

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Successfully uploaded file to Cloudinary: folder={}, url={}", folder, secureUrl);
            return secureUrl;
        } catch (Exception e) {
            log.error("Cloudinary upload failed for folder {}: {}", folder, e.getMessage());
            throw new IOException("Failed to upload media to Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Upload driver personal photo
     */
    public String uploadDriverPhoto(MultipartFile file, String driverId) throws IOException {
        return uploadImage(file, "drivers/" + driverId + "/photo");
    }

    /**
     * Upload vehicle photo
     */
    public String uploadVehiclePhoto(MultipartFile file, String vehicleId) throws IOException {
        return uploadImage(file, "vehicles/" + vehicleId + "/photo");
    }

    /**
     * Upload vehicle compliance document (RC, Insurance, Permit)
     */
    public String uploadVehicleDocument(MultipartFile file, String docType, String vehicleId) throws IOException {
        return uploadImage(file, "vehicles/" + vehicleId + "/docs/" + docType);
    }
}

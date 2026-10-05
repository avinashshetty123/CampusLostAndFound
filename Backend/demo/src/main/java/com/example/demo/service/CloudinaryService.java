package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.controller.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            log.warn("Cloudinary is not configured — image uploads are disabled");
            this.cloudinary = null;
        } else {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
            ));
        }
    }

    public String uploadImage(MultipartFile file) {
        if (cloudinary == null) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Image uploads are not configured on the server");
        }
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Image file is empty");
        }
        String type = file.getContentType();
        if (type != null && !type.startsWith("image/") && !type.equals("application/octet-stream")) {
            throw ApiException.badRequest("Only image files are allowed");
        }
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "campus-laf",
                    "resource_type", "image",
                    // Downscale huge phone photos on Cloudinary's side
                    "transformation", new Transformation<>().crop("limit").width(1600).height(1600).quality("auto")
            ));
            return (String) result.get("secure_url");
        } catch (IOException | RuntimeException e) {
            log.error("Cloudinary upload failed", e);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Image upload failed. Please try again.");
        }
    }
}

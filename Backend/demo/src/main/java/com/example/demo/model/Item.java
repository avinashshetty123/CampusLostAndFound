package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "items")
public class Item {

    @Id
    private String id;

    private String title;
    private String description;
    private String location;
    private String status;          // "LOST" | "FOUND"
    private String category;
    private String imageUrl;
    private String contactInfo;

    // Reporter snapshot (denormalized so item detail works even if user updates profile)
    private String reportedBy;      // User ID
    private String reporterName;
    private String reporterEmail;
    private String reporterPhone;
    private String reporterDept;
    private String reporterClass;

    private boolean isResolved = false;

    @CreatedDate
    private Instant createdAt;
}

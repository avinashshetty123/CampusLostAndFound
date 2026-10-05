package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@Document(collection = "items")
public class Item {

    @Id
    private String id;

    private String title;
    private String description;
    private String location;
    @Indexed
    private String status;          // "LOST" | "FOUND"
    private String category;
    private String imageUrl;
    private String contactInfo;

    // Reporter snapshot (denormalized so item detail works even if user updates profile)
    @Indexed
    private String reportedBy;      // User ID
    private String reporterName;
    private String reporterEmail;
    private String reporterPhone;
    private String reporterDept;
    private String reporterClass;

    // Stored as "isResolved" to stay compatible with existing documents
    @Field("isResolved")
    private boolean resolved = false;
    private Instant resolvedAt;

    // Who claimed / returned the item, and what they said
    private String claimedBy;
    private String claimedByName;
    private String claimMessage;

    @CreatedDate
    @Indexed
    private Instant createdAt;
}

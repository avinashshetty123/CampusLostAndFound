package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    private String password;
    private String mobile;
    private String studentClass;
    private String department;
    private String avatarUrl;

    private int reportsCount = 0;
    private int resolvedCount = 0;

    // IDs of items this user has saved/bookmarked
    private List<String> savedItemIds = new ArrayList<>();

    private boolean notificationsEnabled = true;
}

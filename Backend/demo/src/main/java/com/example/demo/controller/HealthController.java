package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    /** GET /api/ — lightweight ping used by the app to wake a sleeping Render instance */
    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of("status", "ok", "service", "campus-lost-and-found");
    }
}

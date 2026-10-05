package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class DbExecutorConfig {

    /**
     * Small pool for running independent MongoDB queries in parallel.
     * Each Atlas round trip is network-bound, so overlapping them cuts request latency
     * (the common ForkJoinPool is too small on a 0.1-CPU Render instance).
     */
    @Bean(destroyMethod = "shutdown")
    public ExecutorService dbExecutor() {
        return Executors.newFixedThreadPool(8);
    }
}

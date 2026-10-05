package com.example.demo.repository;

import com.example.demo.model.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ItemRepository extends MongoRepository<Item, String> {

    List<Item> findByReportedByOrderByCreatedAtDesc(String userId);

    long countByReportedBy(String userId);

    long countByReportedByAndResolved(String userId, boolean resolved);

    long countByStatus(String status);

    long countByResolved(boolean resolved);
}

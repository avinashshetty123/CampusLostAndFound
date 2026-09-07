package com.example.demo.repository;

import com.example.demo.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ItemRepository extends MongoRepository<Item, String> {

    Page<Item> findByStatus(String status, Pageable pageable);

    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    Page<Item> findByTitleContainingIgnoreCase(String search, Pageable pageable);

    @Query("{ 'status': ?0, 'title': { $regex: ?1, $options: 'i' } }")
    Page<Item> findByStatusAndTitleContainingIgnoreCase(String status, String search, Pageable pageable);

    List<Item> findByReportedByOrderByCreatedAtDesc(String userId);

    long countByStatus(String status);

    long countByIsResolved(boolean isResolved);
}

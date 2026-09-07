package com.example.demo.controller;

import com.example.demo.dto.Dto.*;
import com.example.demo.model.User;
import com.example.demo.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /** GET /api/items?status=LOST&search=bag&page=1&per_page=20 */
    @GetMapping
    public ResponseEntity<ItemsResponse> getItems(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int per_page) {
        return ResponseEntity.ok(itemService.getItems(status, search, page, per_page, user));
    }

    /** GET /api/items/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<SingleItemResponse> getItem(
            @AuthenticationPrincipal User user,
            @PathVariable String id) {
        return ResponseEntity.ok(itemService.getById(id, user));
    }

    /** POST /api/items — report a new lost/found item */
    @PostMapping
    public ResponseEntity<SingleItemResponse> createItem(
            @AuthenticationPrincipal User user,
            @RequestBody CreateItemRequest req) {
        return ResponseEntity.ok(itemService.createItem(req, user));
    }

    /** POST /api/items/{id}/image — upload item photo to Cloudinary */
    @PostMapping("/{id}/image")
    public ResponseEntity<SingleItemResponse> uploadImage(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @RequestPart("image") MultipartFile image) throws IOException {
        return ResponseEntity.ok(itemService.uploadImage(id, image, user));
    }

    /** POST /api/items/{id}/claim — mark item as found/resolved */
    @PostMapping("/{id}/claim")
    public ResponseEntity<MessageResponse> claimItem(
            @AuthenticationPrincipal User user,
            @PathVariable String id,
            @RequestBody ClaimItemRequest req) {
        return ResponseEntity.ok(itemService.claimItem(id, req, user));
    }

    /** DELETE /api/items/{id} — delete your own item */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteItem(
            @AuthenticationPrincipal User user,
            @PathVariable String id) {
        return ResponseEntity.ok(itemService.deleteItem(id, user));
    }

    /** GET /api/items/stats — global counts for the home banner */
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(itemService.getStats());
    }
}

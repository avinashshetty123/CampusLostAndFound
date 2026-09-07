package com.example.demo.service;

import com.example.demo.dto.Dto.*;
import com.example.demo.model.Item;
import com.example.demo.model.User;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public ItemsResponse getItems(String status, String search, int page, int perPage, User currentUser) {
        PageRequest pageable = PageRequest.of(page - 1, perPage, Sort.by(Sort.Direction.DESC, "createdAt"));
        boolean hasStatus = status != null && !status.isBlank();
        boolean hasSearch = search != null && !search.isBlank();

        Page<Item> result;
        if (hasStatus && hasSearch) {
            result = itemRepository.findByStatusAndTitleContainingIgnoreCase(status, search, pageable);
        } else if (hasStatus) {
            result = itemRepository.findByStatus(status, pageable);
        } else if (hasSearch) {
            result = itemRepository.findByTitleContainingIgnoreCase(search, pageable);
        } else {
            result = itemRepository.findAll(pageable);
        }

        List<ItemDto> items = result.getContent().stream()
                .map(i -> toDto(i, currentUser))
                .collect(Collectors.toList());
        return new ItemsResponse(items, result.getTotalElements(), page, perPage);
    }

    public SingleItemResponse getById(String id, User currentUser) {
        Item item = findOrThrow(id);
        return new SingleItemResponse(toDto(item, currentUser), null);
    }

    public SingleItemResponse createItem(CreateItemRequest req, User reporter) {
        Item item = new Item();
        item.setTitle(req.getTitle());
        item.setDescription(req.getDescription());
        item.setLocation(req.getLocation());
        item.setStatus(req.getStatus());
        item.setCategory(req.getCategory());
        item.setContactInfo(req.getContactInfo());

        // Snapshot reporter info so item detail always shows correct data
        item.setReportedBy(reporter.getId());
        item.setReporterName(reporter.getName());
        item.setReporterEmail(reporter.getEmail());
        item.setReporterPhone(reporter.getMobile());
        item.setReporterDept(reporter.getDepartment());
        item.setReporterClass(reporter.getStudentClass());

        itemRepository.save(item);

        reporter.setReportsCount(reporter.getReportsCount() + 1);
        userRepository.save(reporter);

        return new SingleItemResponse(toDto(item, reporter), "Item reported successfully");
    }

    public SingleItemResponse uploadImage(String itemId, MultipartFile file, User user) throws IOException {
        Item item = findOrThrow(itemId);
        if (!item.getReportedBy().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        item.setImageUrl(cloudinaryService.uploadImage(file));
        itemRepository.save(item);
        return new SingleItemResponse(toDto(item, user), "Image uploaded");
    }

    public MessageResponse claimItem(String itemId, ClaimItemRequest req, User user) {
        Item item = findOrThrow(itemId);
        item.setResolved(true);
        itemRepository.save(item);

        userRepository.findById(item.getReportedBy()).ifPresent(reporter -> {
            reporter.setResolvedCount(reporter.getResolvedCount() + 1);
            userRepository.save(reporter);
        });
        return new MessageResponse("Item marked as resolved", true);
    }

    public MessageResponse deleteItem(String itemId, User user) {
        Item item = findOrThrow(itemId);
        if (!item.getReportedBy().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        itemRepository.delete(item);
        user.setReportsCount(Math.max(0, user.getReportsCount() - 1));
        userRepository.save(user);
        return new MessageResponse("Item deleted", true);
    }

    public ItemsResponse getMyItems(User user) {
        List<ItemDto> items = itemRepository.findByReportedByOrderByCreatedAtDesc(user.getId())
                .stream().map(i -> toDto(i, user)).collect(Collectors.toList());
        return new ItemsResponse(items, items.size(), 1, items.size());
    }

    public ItemsResponse getSavedItems(User user) {
        List<ItemDto> items = itemRepository.findAllById(user.getSavedItemIds())
                .stream().map(i -> toDto(i, user)).collect(Collectors.toList());
        return new ItemsResponse(items, items.size(), 1, items.size());
    }

    public MessageResponse saveItem(String itemId, User user) {
        findOrThrow(itemId); // validate item exists
        if (!user.getSavedItemIds().contains(itemId)) {
            user.getSavedItemIds().add(itemId);
            userRepository.save(user);
        }
        return new MessageResponse("Item saved", true);
    }

    public MessageResponse unsaveItem(String itemId, User user) {
        user.getSavedItemIds().remove(itemId);
        userRepository.save(user);
        return new MessageResponse("Item removed from saved", true);
    }

    public StatsResponse getStats() {
        long total = itemRepository.count();
        long lost = itemRepository.countByStatus("LOST");
        long found = itemRepository.countByStatus("FOUND");
        long resolved = itemRepository.countByIsResolved(true);
        return new StatsResponse(total, lost, found, resolved);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    public ItemDto toDto(Item item, User currentUser) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setLocation(item.getLocation());
        dto.setStatus(item.getStatus());
        dto.setCategory(item.getCategory());
        dto.setImageUrl(item.getImageUrl());
        dto.setContactInfo(item.getContactInfo());
        dto.setReportedBy(item.getReportedBy());
        dto.setReporterName(item.getReporterName());
        dto.setReporterEmail(item.getReporterEmail());
        dto.setReporterPhone(item.getReporterPhone());
        dto.setReporterDept(item.getReporterDept());
        dto.setReporterClass(item.getReporterClass());
        dto.setCreatedAt(item.getCreatedAt() != null ? item.getCreatedAt().toString() : null);
        dto.setResolved(item.isResolved());
        if (currentUser != null) {
            dto.setSaved(currentUser.getSavedItemIds().contains(item.getId()));
        }
        return dto;
    }

    private Item findOrThrow(String id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
    }
}

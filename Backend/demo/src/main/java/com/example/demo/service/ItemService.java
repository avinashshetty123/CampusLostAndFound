package com.example.demo.service;

import com.example.demo.controller.ApiException;
import com.example.demo.dto.Dto.*;
import com.example.demo.model.Item;
import com.example.demo.model.User;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.example.demo.service.AuthService.trimToNull;

@Service
@RequiredArgsConstructor
public class ItemService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final MongoTemplate mongoTemplate;

    public ItemsResponse getItems(String status, String search, int page, int perPage, User currentUser) {
        int safePage = Math.max(page, 1);
        int safePerPage = Math.min(Math.max(perPage, 1), MAX_PAGE_SIZE);

        List<Criteria> filters = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            filters.add(Criteria.where("status").is(status.trim().toUpperCase(Locale.ROOT)));
        }
        if (search != null && !search.isBlank()) {
            // Quote the input so user text is matched literally, never as a regex
            Pattern p = Pattern.compile(Pattern.quote(search.trim()), Pattern.CASE_INSENSITIVE);
            filters.add(new Criteria().orOperator(
                    Criteria.where("title").regex(p),
                    Criteria.where("description").regex(p),
                    Criteria.where("location").regex(p),
                    Criteria.where("category").regex(p)
            ));
        }
        Query query = new Query();
        if (!filters.isEmpty()) query.addCriteria(new Criteria().andOperator(filters.toArray(new Criteria[0])));

        long total = mongoTemplate.count(query, Item.class);
        query.with(PageRequest.of(safePage - 1, safePerPage, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<ItemDto> items = mongoTemplate.find(query, Item.class).stream()
                .map(i -> toDto(i, currentUser))
                .toList();
        return new ItemsResponse(items, total, safePage, safePerPage);
    }

    public SingleItemResponse getById(String id, User currentUser) {
        return new SingleItemResponse(toDto(findOrThrow(id), currentUser), null);
    }

    public SingleItemResponse createItem(CreateItemRequest req, User reporter) {
        Item item = new Item();
        item.setTitle(req.getTitle().trim());
        item.setDescription(trimToNull(req.getDescription()));
        item.setLocation(trimToNull(req.getLocation()));
        item.setStatus(req.getStatus());
        item.setCategory(trimToNull(req.getCategory()));
        item.setContactInfo(trimToNull(req.getContactInfo()));

        // Snapshot reporter info so item detail always shows correct data
        item.setReportedBy(reporter.getId());
        item.setReporterName(reporter.getName());
        item.setReporterEmail(reporter.getEmail());
        item.setReporterPhone(reporter.getMobile());
        item.setReporterDept(reporter.getDepartment());
        item.setReporterClass(reporter.getStudentClass());

        itemRepository.save(item);
        return new SingleItemResponse(toDto(item, reporter), "Item reported successfully");
    }

    public SingleItemResponse uploadImage(String itemId, MultipartFile file, User user) {
        Item item = findOrThrow(itemId);
        requireOwner(item, user);
        item.setImageUrl(cloudinaryService.uploadImage(file));
        itemRepository.save(item);
        return new SingleItemResponse(toDto(item, user), "Image uploaded");
    }

    /**
     * Resolves an item. The reporter can mark their own item resolved; anyone else
     * "claims" it (I found it / it's mine) and their message is stored for the reporter.
     */
    public MessageResponse claimItem(String itemId, ClaimItemRequest req, User user) {
        Item item = findOrThrow(itemId);
        if (item.isResolved()) {
            throw ApiException.conflict("This item has already been resolved");
        }
        boolean isOwner = user.getId().equals(item.getReportedBy());
        item.setResolved(true);
        item.setResolvedAt(Instant.now());
        if (!isOwner) {
            item.setClaimedBy(user.getId());
            item.setClaimedByName(user.getName());
            item.setClaimMessage(req == null ? null : trimToNull(req.getMessage()));
        }
        itemRepository.save(item);
        return new MessageResponse(isOwner ? "Marked as resolved" : "Claim sent — the reporter can now see your message", true);
    }

    public MessageResponse deleteItem(String itemId, User user) {
        Item item = findOrThrow(itemId);
        requireOwner(item, user);
        itemRepository.delete(item);
        // Drop the deleted item from everyone's bookmarks
        mongoTemplate.updateMulti(
                Query.query(Criteria.where("savedItemIds").is(itemId)),
                new Update().pull("savedItemIds", itemId),
                User.class);
        return new MessageResponse("Item deleted", true);
    }

    public ItemsResponse getMyItems(User user) {
        List<ItemDto> items = itemRepository.findByReportedByOrderByCreatedAtDesc(user.getId())
                .stream().map(i -> toDto(i, user)).toList();
        return new ItemsResponse(items, items.size(), 1, items.size());
    }

    public ItemsResponse getSavedItems(User user) {
        List<ItemDto> items = itemRepository.findAllById(user.getSavedItemIds()).stream()
                .sorted((a, b) -> compareCreatedDesc(a, b))
                .map(i -> toDto(i, user))
                .toList();
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
        if (user.getSavedItemIds().remove(itemId)) {
            userRepository.save(user);
        }
        return new MessageResponse("Item removed from saved", true);
    }

    public StatsResponse getStats() {
        long total = itemRepository.count();
        long lost = itemRepository.countByStatus("LOST");
        long found = itemRepository.countByStatus("FOUND");
        long resolved = itemRepository.countByResolved(true);
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
        dto.setResolvedAt(item.getResolvedAt() != null ? item.getResolvedAt().toString() : null);
        dto.setClaimedByName(item.getClaimedByName());
        // Only the reporter and the claimer get to read the private claim message
        if (currentUser != null && (currentUser.getId().equals(item.getReportedBy())
                || currentUser.getId().equals(item.getClaimedBy()))) {
            dto.setClaimMessage(item.getClaimMessage());
        }
        if (currentUser != null) {
            dto.setSaved(currentUser.getSavedItemIds().contains(item.getId()));
        }
        return dto;
    }

    private Item findOrThrow(String id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Item not found"));
    }

    private static void requireOwner(Item item, User user) {
        if (!user.getId().equals(item.getReportedBy())) {
            throw ApiException.forbidden("Only the person who reported this item can do that");
        }
    }

    private static int compareCreatedDesc(Item a, Item b) {
        if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
        return b.getCreatedAt().compareTo(a.getCreatedAt());
    }
}

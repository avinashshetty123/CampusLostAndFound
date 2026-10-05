package com.example.demo.service;

import com.example.demo.controller.ApiException;
import com.example.demo.dto.Dto.*;
import com.example.demo.model.User;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest req) {
        String email = normalizeEmail(req.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("Email already registered");
        }
        User user = new User();
        user.setName(req.getName().trim());
        user.setEmail(email);
        user.setMobile(trimToNull(req.getMobile()));
        user.setStudentClass(trimToNull(req.getStudentClass()));
        user.setDepartment(trimToNull(req.getDepartment()));
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        try {
            userRepository.save(user);
        } catch (DuplicateKeyException e) {
            throw ApiException.conflict("Email already registered");
        }
        return new AuthResponse(jwtUtil.generateToken(user.getId(), user.getEmail()), toDto(user), "Registration successful");
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(normalizeEmail(req.getEmail()))
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw ApiException.unauthorized("Invalid email or password");
        }
        return new AuthResponse(jwtUtil.generateToken(user.getId(), user.getEmail()), toDto(user), "Login successful");
    }

    public MessageResponse changePassword(ChangePasswordRequest req, User user) {
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw ApiException.badRequest("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return new MessageResponse("Password changed successfully", true);
    }

    /** Builds the user DTO with live counts so they never drift from the items collection. */
    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setMobile(user.getMobile());
        dto.setStudentClass(user.getStudentClass());
        dto.setDepartment(user.getDepartment());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setNotificationsEnabled(user.isNotificationsEnabled());

        long reports = itemRepository.countByReportedBy(user.getId());
        long resolved = itemRepository.countByReportedByAndResolved(user.getId(), true);
        dto.setReportsCount((int) reports);
        dto.setResolvedCount((int) resolved);
        dto.setPendingCount((int) (reports - resolved));
        return dto;
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

package com.example.demo.service;

import com.example.demo.dto.Dto.*;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setMobile(req.getMobile());
        user.setStudentClass(req.getStudentClass());
        user.setDepartment(req.getDepartment());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);
        return new AuthResponse(jwtUtil.generateToken(user.getId(), user.getEmail()), toDto(user), "Registration successful");
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        return new AuthResponse(jwtUtil.generateToken(user.getId(), user.getEmail()), toDto(user), "Login successful");
    }

    public MessageResponse changePassword(ChangePasswordRequest req, User user) {
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return new MessageResponse("Password changed successfully", true);
    }

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setMobile(user.getMobile());
        dto.setStudentClass(user.getStudentClass());
        dto.setDepartment(user.getDepartment());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setReportsCount(user.getReportsCount());
        dto.setResolvedCount(user.getResolvedCount());
        dto.setNotificationsEnabled(user.isNotificationsEnabled());
        return dto;
    }
}

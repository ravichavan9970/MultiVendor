package com.multivendor.service;

import com.multivendor.dto.UserDtos.*;
import com.multivendor.exception.BadRequestException;
import com.multivendor.exception.ResourceNotFoundException;
import com.multivendor.model.Role;
import com.multivendor.model.User;
import com.multivendor.model.VendorProfile;
import com.multivendor.repository.UserRepository;
import com.multivendor.repository.VendorProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, VendorProfileRepository vendorProfileRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.vendorProfileRepository = vendorProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserProfileResponse getCurrentUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Long vendorId = null;
        String businessName = null;
        String category = null;
        String bio = null;
        String location = null;
        var hourlyRate = java.math.BigDecimal.ZERO;

        if (user.getRole() == Role.VENDOR || user.getRole() == Role.ADMIN) {
            Optional<VendorProfile> profile = vendorProfileRepository.findByUserId(user.getId());
            if (profile.isPresent()) {
                VendorProfile vp = profile.get();
                vendorId = vp.getId();
                businessName = vp.getBusinessName();
                category = vp.getCategory();
                bio = vp.getBio();
                location = vp.getLocation();
                hourlyRate = vp.getHourlyRate();
            }
        }

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getRole(),
                vendorId,
                businessName,
                category,
                bio,
                location,
                hourlyRate
        );
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        userRepository.save(user);

        if (user.getRole() == Role.VENDOR || user.getRole() == Role.ADMIN) {
            Optional<VendorProfile> profileOpt = vendorProfileRepository.findByUserId(user.getId());
            if (profileOpt.isPresent()) {
                VendorProfile vp = profileOpt.get();
                if (request.getBusinessName() != null) vp.setBusinessName(request.getBusinessName());
                if (request.getCategory() != null) vp.setCategory(request.getCategory());
                if (request.getBio() != null) vp.setBio(request.getBio());
                if (request.getLocation() != null) vp.setLocation(request.getLocation());
                if (request.getHourlyRate() != null) vp.setHourlyRate(request.getHourlyRate());
                vendorProfileRepository.save(vp);
            }
        }

        return getCurrentUserProfile(userId);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUserAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        vendorProfileRepository.findByUserId(userId).ifPresent(vendorProfileRepository::delete);
        userRepository.delete(user);
    }
}

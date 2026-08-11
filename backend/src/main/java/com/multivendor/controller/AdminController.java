package com.multivendor.controller;

import com.multivendor.config.DataInitializer;
import com.multivendor.model.User;
import com.multivendor.model.VendorProfile;
import com.multivendor.repository.UserRepository;
import com.multivendor.repository.VendorProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final DataInitializer dataInitializer;

    public AdminController(UserRepository userRepository,
                           VendorProfileRepository vendorProfileRepository,
                           DataInitializer dataInitializer) {
        this.userRepository = userRepository;
        this.vendorProfileRepository = vendorProfileRepository;
        this.dataInitializer = dataInitializer;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/vendors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VendorProfile>> getAllVendorProfiles() {
        return ResponseEntity.ok(vendorProfileRepository.findAll());
    }

    @PutMapping("/vendors/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VendorProfile> approveVendor(@PathVariable Long id, @RequestParam Boolean approve) {
        VendorProfile profile = vendorProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        profile.setIsApproved(approve);
        return ResponseEntity.ok(vendorProfileRepository.save(profile));
    }

    @GetMapping("/dashboard-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalUsers", userRepository.count());
        summary.put("totalVendors", vendorProfileRepository.count());
        summary.put("platformFeePercentage", "10%");
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/reset-system")
    public ResponseEntity<Map<String, String>> resetSystem() {
        dataInitializer.resetAllData();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Full system reset completed! Wiped all bookings, earnings, and custom users.");
        return ResponseEntity.ok(response);
    }
}

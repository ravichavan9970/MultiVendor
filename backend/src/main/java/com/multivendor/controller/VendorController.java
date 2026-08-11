package com.multivendor.controller;

import com.multivendor.dto.VendorDtos.*;
import com.multivendor.model.VendorProfile;
import com.multivendor.security.UserPrincipal;
import com.multivendor.service.VendorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vendors")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping
    public ResponseEntity<List<VendorProfile>> getAllVendors() {
        return ResponseEntity.ok(vendorService.getAllVendors());
    }

    @GetMapping("/my-profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VendorProfile> getMyVendorProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(vendorService.getVendorProfileByUserId(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorProfile> getVendorById(@PathVariable Long id) {
        return ResponseEntity.ok(vendorService.getVendorById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VendorProfile> updateVendorProfile(@PathVariable Long id, @RequestBody VendorProfileRequest request) {
        return ResponseEntity.ok(vendorService.updateVendorProfile(id, request));
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RevenueStatsResponse> getVendorRevenueStats(@PathVariable Long id) {
        return ResponseEntity.ok(vendorService.getVendorRevenueStats(id));
    }
}

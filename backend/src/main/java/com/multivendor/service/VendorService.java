package com.multivendor.service;

import com.multivendor.dto.VendorDtos.*;
import com.multivendor.exception.ResourceNotFoundException;
import com.multivendor.model.Booking;
import com.multivendor.model.BookingStatus;
import com.multivendor.model.VendorProfile;
import com.multivendor.repository.BookingRepository;
import com.multivendor.repository.VendorProfileRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class VendorService {

    private final VendorProfileRepository vendorProfileRepository;
    private final BookingRepository bookingRepository;

    public VendorService(VendorProfileRepository vendorProfileRepository, BookingRepository bookingRepository) {
        this.vendorProfileRepository = vendorProfileRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<VendorProfile> getAllVendors() {
        return vendorProfileRepository.findByIsApprovedTrue();
    }

    public VendorProfile getVendorById(Long id) {
        return vendorProfileRepository.findById(id)
                .orElseGet(() -> vendorProfileRepository.findByUserId(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found with ID or User ID: " + id)));
    }

    public VendorProfile getVendorProfileByUserId(Long userId) {
        return vendorProfileRepository.findByUserId(userId)
                .orElseGet(() -> vendorProfileRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found for user ID: " + userId)));
    }

    public VendorProfile updateVendorProfile(Long vendorId, VendorProfileRequest request) {
        VendorProfile profile = getVendorById(vendorId);
        if (request.getBusinessName() != null) profile.setBusinessName(request.getBusinessName());
        if (request.getCategory() != null) profile.setCategory(request.getCategory());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getLocation() != null) profile.setLocation(request.getLocation());
        if (request.getHourlyRate() != null) profile.setHourlyRate(request.getHourlyRate());

        return vendorProfileRepository.save(profile);
    }

    public RevenueStatsResponse getVendorRevenueStats(Long vendorId) {
        VendorProfile profile = getVendorById(vendorId);
        List<Booking> bookings = bookingRepository.findByVendorId(vendorId);

        BigDecimal totalRevenue = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.COMPLETED)
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long confirmedCount = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.COMPLETED)
                .count();

        long pendingCount = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.HOLD_PENDING_PAYMENT)
                .count();

        return new RevenueStatsResponse(
                profile.getId(),
                profile.getBusinessName(),
                totalRevenue,
                confirmedCount,
                pendingCount,
                profile.getRating().doubleValue()
        );
    }
}

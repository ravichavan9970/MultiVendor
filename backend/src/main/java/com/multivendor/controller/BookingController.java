package com.multivendor.controller;

import com.multivendor.dto.BookingDtos.*;
import com.multivendor.security.UserPrincipal;
import com.multivendor.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/hold")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponse> createBookingHold(@AuthenticationPrincipal UserPrincipal currentUser,
                                                             @RequestHeader(value = "Origin", required = false) String originHeader,
                                                             @RequestHeader(value = "Referer", required = false) String refererHeader,
                                                             @Valid @RequestBody BookingHoldRequest request) {
        String originUrl = originHeader;
        if ((originUrl == null || originUrl.isBlank()) && refererHeader != null && !refererHeader.isBlank()) {
            try {
                java.net.URI uri = new java.net.URI(refererHeader);
                originUrl = uri.getScheme() + "://" + uri.getAuthority();
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(bookingService.createBookingHold(currentUser.getId(), request, originUrl));
    }

    @PostMapping("/confirm-mock")
    public ResponseEntity<BookingResponse> confirmMockBooking(@RequestBody MockWebhookRequest request) {
        return ResponseEntity.ok(bookingService.confirmPaymentAndBooking(request.getBookingReference()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id,
                                                         @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, currentUser.getId()));
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingResponse>> getMyBookings(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(bookingService.getCustomerBookings(currentUser.getId()));
    }

    @GetMapping("/vendor/{vendorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingResponse>> getVendorBookings(@PathVariable Long vendorId) {
        return ResponseEntity.ok(bookingService.getVendorBookings(vendorId));
    }
}

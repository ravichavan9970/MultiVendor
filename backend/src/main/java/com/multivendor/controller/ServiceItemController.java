package com.multivendor.controller;

import com.multivendor.dto.ServiceDtos.*;
import com.multivendor.dto.SlotDtos.*;
import com.multivendor.model.AvailabilitySlot;
import com.multivendor.model.ServiceItem;
import com.multivendor.security.UserPrincipal;
import com.multivendor.service.ServiceItemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
public class ServiceItemController {

    private final ServiceItemService serviceItemService;

    public ServiceItemController(ServiceItemService serviceItemService) {
        this.serviceItemService = serviceItemService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceItem>> getAllServices(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(serviceItemService.getServicesByCategory(category));
        }
        return ResponseEntity.ok(serviceItemService.getAllActiveServices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceItem> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceItemService.getServiceById(id));
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<List<AvailabilitySlot>> getServiceSlots(@PathVariable Long id) {
        return ResponseEntity.ok(serviceItemService.getAvailableSlotsForService(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceItem> createService(@AuthenticationPrincipal UserPrincipal currentUser,
                                                     @Valid @RequestBody ServiceItemRequest request) {
        return ResponseEntity.ok(serviceItemService.createService(currentUser != null ? currentUser.getId() : 1L, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceItem> updateService(@PathVariable Long id,
                                                     @Valid @RequestBody ServiceItemRequest request) {
        return ResponseEntity.ok(serviceItemService.updateService(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceItemService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAllServices() {
        serviceItemService.deleteAllServices();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/slot")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AvailabilitySlot> createSingleSlot(@Valid @RequestBody CreateSlotRequest request) {
        return ResponseEntity.ok(serviceItemService.createSingleSlot(request));
    }

    @PostMapping("/batch-slots")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AvailabilitySlot>> batchGenerateSlots(@Valid @RequestBody BatchSlotGeneratorRequest request) {
        return ResponseEntity.ok(serviceItemService.batchGenerateSlots(request));
    }
}

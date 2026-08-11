package com.multivendor.service;

import com.multivendor.dto.ServiceDtos.*;
import com.multivendor.dto.SlotDtos.*;
import com.multivendor.exception.ResourceNotFoundException;
import com.multivendor.model.AvailabilitySlot;
import com.multivendor.model.ServiceItem;
import com.multivendor.model.SlotStatus;
import com.multivendor.model.User;
import com.multivendor.model.VendorProfile;
import com.multivendor.repository.AvailabilitySlotRepository;
import com.multivendor.repository.ServiceItemRepository;
import com.multivendor.repository.UserRepository;
import com.multivendor.repository.VendorProfileRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceItemService {

    private final ServiceItemRepository serviceItemRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final UserRepository userRepository;

    public ServiceItemService(ServiceItemRepository serviceItemRepository,
                              VendorProfileRepository vendorProfileRepository,
                              AvailabilitySlotRepository availabilitySlotRepository,
                              UserRepository userRepository) {
        this.serviceItemRepository = serviceItemRepository;
        this.vendorProfileRepository = vendorProfileRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.userRepository = userRepository;
    }

    public List<ServiceItem> getAllActiveServices() {
        return serviceItemRepository.findByIsActiveTrue();
    }

    public List<ServiceItem> getServicesByCategory(String category) {
        return serviceItemRepository.findByCategoryAndIsActiveTrue(category);
    }

    public ServiceItem getServiceById(Long id) {
        return serviceItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service item not found with ID: " + id));
    }

    public ServiceItem createService(Long userId, ServiceItemRequest request) {
        // Strict lookup by user ID to prevent mapping to wrong vendor profile
        VendorProfile vendor = vendorProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
                    VendorProfile newVendor = new VendorProfile(
                            user,
                            user.getFullName() + "'s Academy",
                            request.getCategory(),
                            user.getFullName() + " Expert Provider",
                            "Remote",
                            BigDecimal.valueOf(85.00)
                    );
                    return vendorProfileRepository.save(newVendor);
                });

        String meetingLink = (request.getMeetingLink() != null && !request.getMeetingLink().isBlank()) 
                ? request.getMeetingLink().trim() 
                : "https://meet.google.com/dae-zpiu-oau";

        ServiceItem item = new ServiceItem(
                vendor,
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getDurationMinutes(),
                request.getPrice()
        );
        item.setMeetingLink(meetingLink);

        return serviceItemRepository.save(item);
    }

    public ServiceItem updateService(Long serviceId, ServiceItemRequest request) {
        ServiceItem item = getServiceById(serviceId);
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setDurationMinutes(request.getDurationMinutes());
        item.setPrice(request.getPrice());
        if (request.getMeetingLink() != null && !request.getMeetingLink().isBlank()) {
            item.setMeetingLink(request.getMeetingLink().trim());
        }
        return serviceItemRepository.save(item);
    }

    public void deleteService(Long serviceId) {
        ServiceItem item = getServiceById(serviceId);
        item.setIsActive(false);
        serviceItemRepository.save(item);
    }

    public void deleteAllServices() {
        List<ServiceItem> activeServices = serviceItemRepository.findByIsActiveTrue();
        for (ServiceItem item : activeServices) {
            item.setIsActive(false);
        }
        serviceItemRepository.saveAll(activeServices);
    }

    public AvailabilitySlot createSingleSlot(CreateSlotRequest request) {
        ServiceItem service = getServiceById(request.getServiceId());
        AvailabilitySlot slot = new AvailabilitySlot(service, request.getStartTime(), request.getEndTime());
        return availabilitySlotRepository.save(slot);
    }

    public List<AvailabilitySlot> getAvailableSlotsForService(Long serviceId) {
        List<AvailabilitySlot> slots = availabilitySlotRepository.findByServiceIdAndStatusOrderByStartTimeAsc(serviceId, SlotStatus.AVAILABLE);
        if (slots == null || slots.isEmpty()) {
            ServiceItem service = getServiceById(serviceId);
            LocalDateTime now = LocalDateTime.now();
            List<AvailabilitySlot> fallbackSlots = List.of(
                    new AvailabilitySlot(service, now.plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0), now.plusDays(1).withHour(11).withMinute(0).withSecond(0).withNano(0)),
                    new AvailabilitySlot(service, now.plusDays(2).withHour(14).withMinute(0).withSecond(0).withNano(0), now.plusDays(2).withHour(15).withMinute(0).withSecond(0).withNano(0)),
                    new AvailabilitySlot(service, now.plusDays(3).withHour(16).withMinute(0).withSecond(0).withNano(0), now.plusDays(3).withHour(17).withMinute(0).withSecond(0).withNano(0))
            );
            return availabilitySlotRepository.saveAll(fallbackSlots);
        }
        return slots;
    }

    public List<AvailabilitySlot> batchGenerateSlots(BatchSlotGeneratorRequest request) {
        ServiceItem service = getServiceById(request.getServiceId());
        List<AvailabilitySlot> createdSlots = new ArrayList<>();

        LocalDateTime currentDay = request.getStartDate();
        for (int d = 0; d < request.getDaysCount(); d++) {
            LocalDateTime slotStart = currentDay.withHour(request.getStartHour()).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime dayEnd = currentDay.withHour(request.getEndHour()).withMinute(0).withSecond(0).withNano(0);

            LocalDateTime nextSlotEnd = slotStart.plusMinutes(request.getSlotDurationMinutes());
            while (!nextSlotEnd.isAfter(dayEnd)) {
                AvailabilitySlot slot = new AvailabilitySlot(service, slotStart, nextSlotEnd);
                createdSlots.add(slot);

                slotStart = nextSlotEnd;
                nextSlotEnd = slotStart.plusMinutes(request.getSlotDurationMinutes());
            }

            currentDay = currentDay.plusDays(1);
        }

        return availabilitySlotRepository.saveAll(createdSlots);
    }
}

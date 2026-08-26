package com.multivendor.config;

import com.multivendor.model.*;
import com.multivendor.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           VendorProfileRepository vendorProfileRepository,
                           ServiceItemRepository serviceItemRepository,
                           AvailabilitySlotRepository availabilitySlotRepository,
                           BookingRepository bookingRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.vendorProfileRepository = vendorProfileRepository;
        this.serviceItemRepository = serviceItemRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("🚀 Database is empty. Initializing seed data and demo accounts...");
            initSeedData();
        } else {
            log.info("💾 Existing database detected with {} users. Permanent data storage preserved.", userRepository.count());
            ensureSlotsAvailableForActiveServices();
        }
    }

    private void initSeedData() {
        String encodedPassword = passwordEncoder.encode("password123");

        // 1. Admin
        User admin = new User("admin@multivendor.com", encodedPassword, "System Administrator", "+1 5550201", Role.ADMIN);
        userRepository.save(admin);

        // 2. Customer
        User customer = new User("customer.john@gmail.com", encodedPassword, "John Doe", "+1 5550209", Role.CUSTOMER);
        userRepository.save(customer);

        // 3. Registered Vendor Accounts (Clean Slate with 0 Default Services)
        createVendorWithServices(
                "vendor.alex@multivendor.com", encodedPassword, "Alex Rivera", "+1 5550202",
                "Rivera Tech & Algorithms Academy", "TUTORING",
                "Senior Software Engineer coaching Java, Data Structures, and System Design.",
                "San Francisco, CA (Remote)", BigDecimal.valueOf(85.00),
                List.of()
        );

        createVendorWithServices(
                "vendor.sarah@multivendor.com", encodedPassword, "Sarah Chen", "+1 5550203",
                "PixelCraft Digital Design Studio", "FREELANCE",
                "Award-winning UI/UX designer specializing in modern Web App interfaces and design systems.",
                "New York, NY (Remote)", BigDecimal.valueOf(95.00),
                List.of()
        );

        createVendorWithServices(
                "vendor.marcus@multivendor.com", encodedPassword, "Marcus Vance", "+1 5550204",
                "Apex Cloud & DevOps Engineering Lab", "CONSULTING",
                "AWS Certified Solutions Architect helping startups scale cloud infrastructure.",
                "Austin, TX (Remote)", BigDecimal.valueOf(120.00),
                List.of()
        );

        log.info("✅ Database initialization complete! Demo accounts ready with zero default services.");
    }

    private void createVendorWithServices(String email, String password, String fullName, String phone,
                                         String businessName, String category, String bio, String location,
                                         BigDecimal rate, List<ServiceSeed> services) {
        User user = new User(email, password, fullName, phone, Role.VENDOR);
        User savedUser = userRepository.save(user);

        VendorProfile profile = new VendorProfile(savedUser, businessName, category, bio, location, rate);
        profile.setRating(BigDecimal.valueOf(4.90));
        profile.setTotalReviews(35);
        profile.setIsApproved(true);
        VendorProfile savedProfile = vendorProfileRepository.save(profile);

        for (ServiceSeed s : services) {
            ServiceItem item = new ServiceItem(savedProfile, s.title, s.description, s.category, s.durationMinutes, s.price, s.meetingLink);
            ServiceItem savedItem = serviceItemRepository.save(item);

            // Generate slots for upcoming 5 days (10:00, 14:00, 16:00, 18:00 each day)
            generateSlotsForService(savedItem);
        }
    }

    private void generateSlotsForService(ServiceItem service) {
        LocalDateTime now = LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0);
        int[] hours = {10, 14, 16, 18};

        for (int day = 0; day < 5; day++) {
            LocalDateTime dayBase = now.plusDays(day);
            for (int hour : hours) {
                LocalDateTime startTime = dayBase.withHour(hour);
                LocalDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());
                AvailabilitySlot slot = new AvailabilitySlot(service, startTime, endTime);
                slot.setStatus(SlotStatus.AVAILABLE);
                availabilitySlotRepository.save(slot);
            }
        }
    }

    private void ensureSlotsAvailableForActiveServices() {
        List<ServiceItem> activeServices = serviceItemRepository.findAll().stream().filter(ServiceItem::getIsActive).toList();
        for (ServiceItem s : activeServices) {
            List<AvailabilitySlot> existing = availabilitySlotRepository.findByServiceId(s.getId());
            long availableCount = existing.stream().filter(slot -> slot.getStatus() == SlotStatus.AVAILABLE).count();
            if (availableCount < 3) {
                generateSlotsForService(s);
                log.info("⚡ Generated fresh availability slots for service: {}", s.getTitle());
            }
        }
    }

    public void clearAllServices() {
        log.info("🧹 Wiping all services, availability slots, and bookings...");
        bookingRepository.deleteAll();
        availabilitySlotRepository.deleteAll();
        serviceItemRepository.deleteAll();
        log.info("✅ All services, slots, and bookings deleted cleanly.");
    }

    public void resetAllData() {
        log.info("🧹 Initiating FULL Database Reset from Admin Panel...");
        bookingRepository.deleteAll();
        availabilitySlotRepository.deleteAll();
        serviceItemRepository.deleteAll();
        vendorProfileRepository.deleteAll();
        userRepository.deleteAll();
        initSeedData();
        log.info("✨ Full Reset Complete! Database reseeded with clean accounts and zero default services.");
    }

    private static class ServiceSeed {
        String title;
        String description;
        String category;
        int durationMinutes;
        BigDecimal price;
        String meetingLink;

        ServiceSeed(String title, String description, String category, int durationMinutes, BigDecimal price, String meetingLink) {
            this.title = title;
            this.description = description;
            this.category = category;
            this.durationMinutes = durationMinutes;
            this.price = price;
            this.meetingLink = meetingLink;
        }
    }
}

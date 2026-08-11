package com.multivendor.config;

import com.multivendor.model.*;
import com.multivendor.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final BookingRepository bookingRepository;

    public DataInitializer(UserRepository userRepository,
                           VendorProfileRepository vendorProfileRepository,
                           ServiceItemRepository serviceItemRepository,
                           AvailabilitySlotRepository availabilitySlotRepository,
                           BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.vendorProfileRepository = vendorProfileRepository;
        this.serviceItemRepository = serviceItemRepository;
        this.availabilitySlotRepository = availabilitySlotRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void run(String... args) {
        resetAllData();
    }

    public void resetAllData() {
        log.info("🧹 Initiating FULL Database Reset (Transaction History, Earnings, Bookings & Custom Users)...");

        // 1. Wipe out ALL transaction history & bookings
        bookingRepository.deleteAll();
        log.info("✅ Wiped out all booking transaction history and confirmed earnings.");

        // 2. Reset all availability slot statuses back to AVAILABLE
        List<AvailabilitySlot> allSlots = availabilitySlotRepository.findAll();
        for (AvailabilitySlot slot : allSlots) {
            slot.setStatus(SlotStatus.AVAILABLE);
        }
        availabilitySlotRepository.saveAll(allSlots);
        log.info("✅ Reset all availability slots to AVAILABLE status.");

        // 3. Delete all custom registered user accounts except seed demo users
        Set<String> demoEmails = Set.of(
                "admin@multivendor.com",
                "vendor.alex@multivendor.com",
                "vendor.sarah@multivendor.com",
                "customer.john@gmail.com"
        );

        List<User> customUsers = userRepository.findAll().stream()
                .filter(u -> !demoEmails.contains(u.getEmail().toLowerCase()))
                .toList();

        for (User user : customUsers) {
            // Delete vendor services & profiles created by custom user
            vendorProfileRepository.findByUserId(user.getId()).ifPresent(profile -> {
                List<ServiceItem> userServices = serviceItemRepository.findByVendorIdAndIsActiveTrue(profile.getId());
                for (ServiceItem s : userServices) {
                    availabilitySlotRepository.deleteAll(availabilitySlotRepository.findByServiceId(s.getId()));
                    serviceItemRepository.delete(s);
                }
                vendorProfileRepository.delete(profile);
            });

            userRepository.delete(user);
            log.info("🗑️ Deleted custom user account & data: {}", user.getEmail());
        }

        // 4. Ensure demo seed users have clean VendorProfiles
        List<User> remainingUsers = userRepository.findAll();
        for (User user : remainingUsers) {
            if (user.getRole() == Role.VENDOR || user.getRole() == Role.ADMIN) {
                if (vendorProfileRepository.findByUserId(user.getId()).isEmpty()) {
                    VendorProfile profile = new VendorProfile(
                            user,
                            user.getFullName() + "'s Academy",
                            "TUTORING",
                            "Verified Expert Provider Overview",
                            "Remote",
                            BigDecimal.valueOf(85.00)
                    );
                    vendorProfileRepository.save(profile);
                    log.info("✅ Created clean VendorProfile for demo user: {}", user.getEmail());
                }
            }
        }

        log.info("✨ Full Reset Complete! All earnings ($0.00), confirmed bookings (0), and transaction history are clear.");
    }
}

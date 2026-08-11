package com.multivendor.concurrency;

import com.multivendor.dto.BookingDtos.BookingHoldRequest;
import com.multivendor.exception.SlotUnavailableException;
import com.multivendor.model.*;
import com.multivendor.repository.*;
import com.multivendor.service.BookingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class BookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private AvailabilitySlotRepository slotRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VendorProfileRepository vendorProfileRepository;

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    @Test
    @DisplayName("Concurrency Lock Test: 10 parallel threads attempting to book the SAME slot simultaneously")
    void testParallelDoubleBookingPrevention() throws InterruptedException {
        long timestamp = System.currentTimeMillis();
        User customer = userRepository.save(new User("concurrent" + timestamp + "@customer.com", "pass", "Concurrent Customer", "123", Role.CUSTOMER));
        User vendorUser = userRepository.save(new User("vendor" + timestamp + "@test.com", "pass", "Vendor User", "123", Role.VENDOR));

        VendorProfile vendor = vendorProfileRepository.save(new VendorProfile(vendorUser, "Concurrency Academy", "TUTORING", "Bio", "Remote", BigDecimal.valueOf(50)));
        ServiceItem service = serviceItemRepository.save(new ServiceItem(vendor, "Concurrency Test Service", "Desc", "TUTORING", 60, BigDecimal.valueOf(50)));
        AvailabilitySlot targetSlot = slotRepository.save(new AvailabilitySlot(service, LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(10).plusHours(1)));

        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        BookingHoldRequest request = new BookingHoldRequest();
        request.setSlotId(targetSlot.getId());

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> {
                try {
                    latch.await(); // Synchronize thread start
                    bookingService.createBookingHold(customer.getId(), request);
                    successCount.incrementAndGet();
                } catch (SlotUnavailableException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Unexpected exception in concurrency test thread: " + e);
                    e.printStackTrace();
                    failureCount.incrementAndGet();
                }
            });
        }

        latch.countDown(); // Unblock all 10 threads simultaneously
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // Exactly 1 thread should succeed in locking the slot, and 9 should fail
        assertEquals(1, successCount.get(), "Exactly ONE thread should successfully hold the slot");
        assertEquals(9, failureCount.get(), "9 threads should be rejected due to slot unavailability lock");
    }
}

package com.multivendor.service;

import com.multivendor.dto.BookingDtos.*;
import com.multivendor.exception.ResourceNotFoundException;
import com.multivendor.exception.SlotUnavailableException;
import com.multivendor.model.*;
import com.multivendor.repository.AvailabilitySlotRepository;
import com.multivendor.repository.BookingRepository;
import com.multivendor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final UserRepository userRepository;
    private final AvailabilitySlotRepository slotRepository;
    private final BookingRepository bookingRepository;
    private final StripeService stripeService;

    public BookingService(UserRepository userRepository,
                          AvailabilitySlotRepository slotRepository,
                          BookingRepository bookingRepository,
                          StripeService stripeService) {
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        this.stripeService = stripeService;
    }

    /**
     * CRITICAL: Acquires a Pessimistic Write Lock on the AvailabilitySlot row in MySQL
     * to eliminate double-booking race conditions when concurrent requests arrive.
     */
    @Transactional
    public BookingResponse createBookingHold(Long customerUserId, BookingHoldRequest request, String originUrl) {
        User customer = userRepository.findById(customerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer user not found with ID: " + customerUserId));

        // SELECT FOR UPDATE (Pessimistic Lock)
        AvailabilitySlot slot = slotRepository.findByIdWithPessimisticLock(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Availability slot not found with ID: " + request.getSlotId()));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new SlotUnavailableException("Slot #" + request.getSlotId() + " is currently unavailable or booked by another customer.");
        }

        // Lock slot -> set status to HOLD
        slot.setStatus(SlotStatus.HOLD);
        slotRepository.save(slot);

        // Reuse existing booking record for this slot if present (prevents unique constraint error on slot_id)
        ServiceItem service = slot.getService();
        Optional<Booking> existingBooking = bookingRepository.findBySlotId(slot.getId());

        Booking booking;
        if (existingBooking.isPresent()) {
            booking = existingBooking.get();
            booking.setCustomer(customer);
            booking.setService(service);
            booking.setTotalAmount(service.getPrice());
            booking.setStatus(BookingStatus.HOLD_PENDING_PAYMENT);
            booking.setHoldExpiresAt(LocalDateTime.now().plusMinutes(10));
            booking.setBookingReference(UUID.randomUUID().toString());
            booking.setUpdatedAt(LocalDateTime.now());
        } else {
            booking = new Booking(customer, service, slot, service.getPrice());
        }

        if (request.getCustomerNotes() != null && !request.getCustomerNotes().isBlank()) {
            booking.setCustomerNotes(request.getCustomerNotes().trim());
        }

        Booking savedBooking = bookingRepository.save(booking);

        // Generate Stripe Checkout URL
        String checkoutUrl = stripeService.createCheckoutSession(savedBooking, originUrl);

        return mapToResponse(savedBooking, checkoutUrl);
    }

    @Transactional
    public BookingResponse createBookingHold(Long customerUserId, BookingHoldRequest request) {
        return createBookingHold(customerUserId, request, null);
    }

    @Transactional
    public BookingResponse confirmPaymentAndBooking(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found for reference: " + bookingReference));

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setUpdatedAt(LocalDateTime.now());

        AvailabilitySlot slot = booking.getSlot();
        slot.setStatus(SlotStatus.BOOKED);

        slotRepository.save(slot);
        Booking updatedBooking = bookingRepository.save(booking);

        log.info("Booking {} CONFIRMED and Slot {} marked BOOKED", bookingReference, slot.getId());
        return mapToResponse(updatedBooking, null);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, Long customerUserId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (!booking.getCustomer().getId().equals(customerUserId)) {
            throw new IllegalArgumentException("Unauthorized to cancel this booking.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdTime = booking.getCreatedAt();
        if (createdTime.plusHours(1).isBefore(now)) {
            throw new IllegalArgumentException("Cancellation policy violation: Bookings can only be cancelled within 1 hour of booking.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(now);

        AvailabilitySlot slot = booking.getSlot();
        if (slot != null) {
            slot.setStatus(SlotStatus.AVAILABLE);
            slotRepository.save(slot);
        }

        Booking updated = bookingRepository.save(booking);
        log.info("Booking {} CANCELLED within 1-hour window. Slot {} released to AVAILABLE", booking.getBookingReference(), slot != null ? slot.getId() : "null");

        return mapToResponse(updated, null);
    }

    public List<BookingResponse> getCustomerBookings(Long customerId) {
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(b -> mapToResponse(b, null))
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getVendorBookings(Long vendorId) {
        return bookingRepository.findByVendorId(vendorId)
                .stream()
                .map(b -> mapToResponse(b, null))
                .collect(Collectors.toList());
    }

    /**
     * Scheduled Background Cleanup Daemon:
     * Runs every 60 seconds to release un-paid booking holds after 10 minutes.
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void releaseExpiredHolds() {
        List<Booking> expiredBookings = bookingRepository.findExpiredHolds(LocalDateTime.now());
        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.getSlot().setStatus(SlotStatus.AVAILABLE);

            slotRepository.save(booking.getSlot());
            bookingRepository.save(booking);
            log.info("Released expired hold for Booking Reference: {}", booking.getBookingReference());
        }
    }

    private BookingResponse mapToResponse(Booking booking, String checkoutUrl) {
        String phone = (booking.getCustomer() != null && booking.getCustomer().getPhoneNumber() != null && !booking.getCustomer().getPhoneNumber().isBlank())
                ? booking.getCustomer().getPhoneNumber() : "Not provided";
        String meetingLink = (booking.getService() != null && booking.getService().getMeetingLink() != null && !booking.getService().getMeetingLink().isBlank())
                ? booking.getService().getMeetingLink() : "https://meet.google.com/dae-zpiu-oau";

        return new BookingResponse(
                booking.getId(),
                booking.getBookingReference(),
                booking.getCustomer().getFullName(),
                booking.getCustomer().getEmail(),
                phone,
                booking.getCustomerNotes(),
                booking.getService().getVendor().getBusinessName(),
                booking.getService().getTitle(),
                booking.getSlot().getStartTime(),
                booking.getSlot().getEndTime(),
                booking.getTotalAmount(),
                booking.getStatus(),
                booking.getHoldExpiresAt(),
                checkoutUrl,
                meetingLink,
                booking.getCreatedAt()
        );
    }
}

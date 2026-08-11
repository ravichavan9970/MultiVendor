package com.multivendor.service;

import com.multivendor.dto.BookingDtos.BookingHoldRequest;
import com.multivendor.dto.BookingDtos.BookingResponse;
import com.multivendor.exception.SlotUnavailableException;
import com.multivendor.model.*;
import com.multivendor.repository.AvailabilitySlotRepository;
import com.multivendor.repository.BookingRepository;
import com.multivendor.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private AvailabilitySlotRepository slotRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StripeService stripeService;

    @InjectMocks
    private BookingService bookingService;

    private User testUser;
    private VendorProfile testVendor;
    private ServiceItem testService;
    private AvailabilitySlot testSlot;

    @BeforeEach
    void setUp() {
        testUser = new User("test@customer.com", "passhash", "Test Customer", "+123456", Role.CUSTOMER);
        testUser.setId(10L);

        User vendorUser = new User("vendor@test.com", "passhash", "Test Vendor", "+123456", Role.VENDOR);
        vendorUser.setId(20L);

        testVendor = new VendorProfile(vendorUser, "Test Academy", "TUTORING", "Bio", "Remote", BigDecimal.valueOf(100.00));
        testVendor.setId(1L);

        testService = new ServiceItem(testVendor, "Java Coaching", "Description", "TUTORING", 60, BigDecimal.valueOf(100.00));
        testService.setId(5L);

        testSlot = new AvailabilitySlot(testService, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1));
        testSlot.setId(101L);
    }

    @Test
    @DisplayName("Should successfully create a booking hold on an available slot")
    void testCreateBookingHoldSuccess() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));
        when(slotRepository.findByIdWithPessimisticLock(101L)).thenReturn(Optional.of(testSlot));
        when(slotRepository.save(any(AvailabilitySlot.class))).thenReturn(testSlot);

        Booking savedBooking = new Booking(testUser, testService, testSlot, testService.getPrice());
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(stripeService.createCheckoutSession(any(Booking.class), any())).thenReturn("https://checkout.stripe.com/test");

        BookingHoldRequest request = new BookingHoldRequest();
        request.setSlotId(101L);

        BookingResponse response = bookingService.createBookingHold(10L, request);

        assertNotNull(response);
        assertEquals(BookingStatus.HOLD_PENDING_PAYMENT, response.getStatus());
        assertEquals("Java Coaching", response.getServiceTitle());
        assertEquals(SlotStatus.HOLD, testSlot.getStatus());

        verify(slotRepository, times(1)).findByIdWithPessimisticLock(101L);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw SlotUnavailableException when slot is already held or booked")
    void testCreateBookingHoldSlotUnavailable() {
        testSlot.setStatus(SlotStatus.BOOKED);
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));
        when(slotRepository.findByIdWithPessimisticLock(101L)).thenReturn(Optional.of(testSlot));

        BookingHoldRequest request = new BookingHoldRequest();
        request.setSlotId(101L);

        assertThrows(SlotUnavailableException.class, () -> bookingService.createBookingHold(10L, request));
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}

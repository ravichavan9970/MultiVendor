package com.multivendor.repository;

import com.multivendor.model.Booking;
import com.multivendor.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    Optional<Booking> findBySlotId(Long slotId);

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("SELECT b FROM Booking b WHERE b.service.vendor.id = :vendorId OR b.service.vendor.user.id = :vendorId OR b.service.vendor.user.id IN (SELECT v.user.id FROM VendorProfile v WHERE v.id = :vendorId) ORDER BY b.createdAt DESC")
    List<Booking> findByVendorId(@Param("vendorId") Long vendorId);

    @Query("SELECT b FROM Booking b WHERE b.status = 'HOLD_PENDING_PAYMENT' AND b.holdExpiresAt < :now")
    List<Booking> findExpiredHolds(@Param("now") LocalDateTime now);
}

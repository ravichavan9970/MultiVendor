package com.multivendor.repository;

import com.multivendor.model.AvailabilitySlot;
import com.multivendor.model.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    List<AvailabilitySlot> findByServiceId(Long serviceId);

    List<AvailabilitySlot> findByServiceIdAndStartTimeAfterOrderByStartTimeAsc(Long serviceId, LocalDateTime now);

    List<AvailabilitySlot> findByServiceIdAndStatusOrderByStartTimeAsc(Long serviceId, SlotStatus status);

    // CRITICAL: Pessimistic write lock query to prevent race conditions during booking hold creation
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AvailabilitySlot s WHERE s.id = :slotId")
    Optional<AvailabilitySlot> findByIdWithPessimisticLock(@Param("slotId") Long slotId);
}

package com.multivendor.repository;

import com.multivendor.model.VendorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorProfileRepository extends JpaRepository<VendorProfile, Long> {
    Optional<VendorProfile> findByUserId(Long userId);
    List<VendorProfile> findByCategoryAndIsApprovedTrue(String category);
    List<VendorProfile> findByIsApprovedTrue();
}

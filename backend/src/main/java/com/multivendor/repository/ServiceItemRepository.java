package com.multivendor.repository;

import com.multivendor.model.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {
    List<ServiceItem> findByIsActiveTrue();
    
    @org.springframework.data.jpa.repository.Query("SELECT s FROM ServiceItem s WHERE s.isActive = true AND (s.vendor.id = :vendorId OR s.vendor.user.id = :vendorId OR s.vendor.user.id IN (SELECT v.user.id FROM VendorProfile v WHERE v.id = :vendorId))")
    List<ServiceItem> findByVendorIdAndIsActiveTrue(@org.springframework.data.repository.query.Param("vendorId") Long vendorId);
    
    List<ServiceItem> findByCategoryAndIsActiveTrue(String category);
}

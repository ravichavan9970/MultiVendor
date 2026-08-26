package com.multivendor.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_items")
public class ServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vendor_id", nullable = false)
    private VendorProfile vendor;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 60;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "meeting_link", length = 500)
    private String meetingLink = "https://meet.google.com/dae-zpiu-oau";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ServiceItem() {}

    public ServiceItem(VendorProfile vendor, String title, String description, String category, Integer durationMinutes, BigDecimal price) {
        this.vendor = vendor;
        this.title = title;
        this.description = description;
        this.category = category;
        this.durationMinutes = durationMinutes;
        this.price = price;
    }

    public ServiceItem(VendorProfile vendor, String title, String description, String category, Integer durationMinutes, BigDecimal price, String meetingLink) {
        this.vendor = vendor;
        this.title = title;
        this.description = description;
        this.category = category;
        this.durationMinutes = durationMinutes;
        this.price = price;
        this.meetingLink = meetingLink;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public VendorProfile getVendor() { return vendor; }
    public void setVendor(VendorProfile vendor) { this.vendor = vendor; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

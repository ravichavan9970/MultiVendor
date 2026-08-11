package com.multivendor.dto;

import java.math.BigDecimal;

public class VendorDtos {

    public static class VendorProfileRequest {
        private String businessName;
        private String category;
        private String bio;
        private String location;
        private BigDecimal hourlyRate;

        public String getBusinessName() { return businessName; }
        public void setBusinessName(String businessName) { this.businessName = businessName; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public BigDecimal getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
    }

    public static class RevenueStatsResponse {
        private Long vendorId;
        private String businessName;
        private BigDecimal totalRevenue;
        private Long totalConfirmedBookings;
        private Long totalPendingBookings;
        private Double averageRating;

        public RevenueStatsResponse(Long vendorId, String businessName, BigDecimal totalRevenue, Long totalConfirmedBookings, Long totalPendingBookings, Double averageRating) {
            this.vendorId = vendorId;
            this.businessName = businessName;
            this.totalRevenue = totalRevenue;
            this.totalConfirmedBookings = totalConfirmedBookings;
            this.totalPendingBookings = totalPendingBookings;
            this.averageRating = averageRating;
        }

        public Long getVendorId() { return vendorId; }
        public String getBusinessName() { return businessName; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public Long getTotalConfirmedBookings() { return totalConfirmedBookings; }
        public Long getTotalPendingBookings() { return totalPendingBookings; }
        public Double getAverageRating() { return averageRating; }
    }
}

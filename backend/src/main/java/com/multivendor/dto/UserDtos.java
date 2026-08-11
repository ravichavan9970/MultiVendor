package com.multivendor.dto;

import com.multivendor.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class UserDtos {

    public static class UserProfileUpdateRequest {
        private String fullName;
        private String phoneNumber;

        // Vendor profile fields (if user is VENDOR)
        private String businessName;
        private String category;
        private String bio;
        private String location;
        private BigDecimal hourlyRate;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

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

    public static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;

        @NotBlank @Size(min = 6)
        private String newPassword;

        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class UserProfileResponse {
        private Long id;
        private String email;
        private String fullName;
        private String phoneNumber;
        private Role role;
        private Long vendorId;
        private String businessName;
        private String category;
        private String bio;
        private String location;
        private BigDecimal hourlyRate;

        public UserProfileResponse(Long id, String email, String fullName, String phoneNumber, Role role,
                                   Long vendorId, String businessName, String category, String bio, String location, BigDecimal hourlyRate) {
            this.id = id;
            this.email = email;
            this.fullName = fullName;
            this.phoneNumber = phoneNumber;
            this.role = role;
            this.vendorId = vendorId;
            this.businessName = businessName;
            this.category = category;
            this.bio = bio;
            this.location = location;
            this.hourlyRate = hourlyRate;
        }

        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public String getPhoneNumber() { return phoneNumber; }
        public Role getRole() { return role; }
        public Long getVendorId() { return vendorId; }
        public String getBusinessName() { return businessName; }
        public String getCategory() { return category; }
        public String getBio() { return bio; }
        public String getLocation() { return location; }
        public BigDecimal getHourlyRate() { return hourlyRate; }
    }
}

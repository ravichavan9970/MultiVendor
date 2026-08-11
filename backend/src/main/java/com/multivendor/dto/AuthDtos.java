package com.multivendor.dto;

import com.multivendor.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public static class LoginRequest {
        @NotBlank
        private String email; // Accepts either email address or mobile phone number

        @NotBlank
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        @NotBlank @Email
        private String email;

        @NotBlank @Size(min = 6)
        private String password;

        @NotBlank
        private String fullName;

        private String phoneNumber;
        private Role role = Role.CUSTOMER;

        // Vendor profile fields if registering as VENDOR
        private String businessName;
        private String category;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }

        public String getBusinessName() { return businessName; }
        public void setBusinessName(String businessName) { this.businessName = businessName; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    public static class VerifyOtpRequest {
        @NotBlank
        private String email;

        @NotBlank @Size(min = 6, max = 6)
        private String otpCode;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getOtpCode() { return otpCode; }
        public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    }

    public static class OtpInitiateResponse {
        private String message;
        private String email;
        private String demoOtpCode;

        public OtpInitiateResponse(String message, String email, String demoOtpCode) {
            this.message = message;
            this.email = email;
            this.demoOtpCode = demoOtpCode;
        }

        public String getMessage() { return message; }
        public String getEmail() { return email; }
        public String getDemoOtpCode() { return demoOtpCode; }
    }

    public static class AuthResponse {
        private String token;
        private Long userId;
        private String email;
        private String fullName;
        private Role role;
        private Long vendorId;

        public AuthResponse(String token, Long userId, String email, String fullName, Role role, Long vendorId) {
            this.token = token;
            this.userId = userId;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
            this.vendorId = vendorId;
        }

        public String getToken() { return token; }
        public Long getUserId() { return userId; }
        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
        public Role getRole() { return role; }
        public Long getVendorId() { return vendorId; }
    }
}

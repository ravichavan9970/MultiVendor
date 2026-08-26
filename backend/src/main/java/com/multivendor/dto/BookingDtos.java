package com.multivendor.dto;

import com.multivendor.model.BookingStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingDtos {

    public static class BookingHoldRequest {
        @NotNull
        private Long slotId;

        private String customerNotes;

        public Long getSlotId() { return slotId; }
        public void setSlotId(Long slotId) { this.slotId = slotId; }

        public String getCustomerNotes() { return customerNotes; }
        public void setCustomerNotes(String customerNotes) { this.customerNotes = customerNotes; }
    }

    public static class BookingResponse {
        private Long id;
        private String bookingReference;
        private String customerName;
        private String customerEmail;
        private String customerPhone;
        private String customerNotes;
        private String vendorBusinessName;
        private String serviceTitle;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private BigDecimal totalAmount;
        private BookingStatus status;
        private LocalDateTime holdExpiresAt;
        private String stripeCheckoutUrl;
        private String meetingLink;
        private String utrNumber;
        private String paymentMethod;
        private LocalDateTime paymentSubmittedAt;
        private LocalDateTime createdAt;

        public BookingResponse(Long id, String bookingReference, String customerName, String customerEmail,
                               String customerPhone, String customerNotes,
                               String vendorBusinessName, String serviceTitle, LocalDateTime startTime,
                               LocalDateTime endTime, BigDecimal totalAmount, BookingStatus status,
                               LocalDateTime holdExpiresAt, String stripeCheckoutUrl, String meetingLink,
                               String utrNumber, String paymentMethod, LocalDateTime paymentSubmittedAt,
                               LocalDateTime createdAt) {
            this.id = id;
            this.bookingReference = bookingReference;
            this.customerName = customerName;
            this.customerEmail = customerEmail;
            this.customerPhone = customerPhone;
            this.customerNotes = customerNotes;
            this.vendorBusinessName = vendorBusinessName;
            this.serviceTitle = serviceTitle;
            this.startTime = startTime;
            this.endTime = endTime;
            this.totalAmount = totalAmount;
            this.status = status;
            this.holdExpiresAt = holdExpiresAt;
            this.stripeCheckoutUrl = stripeCheckoutUrl;
            this.meetingLink = meetingLink;
            this.utrNumber = utrNumber;
            this.paymentMethod = paymentMethod;
            this.paymentSubmittedAt = paymentSubmittedAt;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public String getBookingReference() { return bookingReference; }
        public String getCustomerName() { return customerName; }
        public String getCustomerEmail() { return customerEmail; }
        public String getCustomerPhone() { return customerPhone; }
        public String getCustomerNotes() { return customerNotes; }
        public String getVendorBusinessName() { return vendorBusinessName; }
        public String getServiceTitle() { return serviceTitle; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BookingStatus getStatus() { return status; }
        public LocalDateTime getHoldExpiresAt() { return holdExpiresAt; }
        public String getStripeCheckoutUrl() { return stripeCheckoutUrl; }
        public String getMeetingLink() { return meetingLink; }
        public String getUtrNumber() { return utrNumber; }
        public String getPaymentMethod() { return paymentMethod; }
        public LocalDateTime getPaymentSubmittedAt() { return paymentSubmittedAt; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    public static class SubmitUtrRequest {
        @NotNull
        private String bookingReference;

        @NotNull
        private String utrNumber;

        private String notes;

        public String getBookingReference() { return bookingReference; }
        public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

        public String getUtrNumber() { return utrNumber; }
        public void setUtrNumber(String utrNumber) { this.utrNumber = utrNumber; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class VerifyUtrRequest {
        @NotNull
        private String bookingReference;

        private boolean approve = true;

        private String rejectionReason;

        public String getBookingReference() { return bookingReference; }
        public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

        public boolean isApprove() { return approve; }
        public void setApprove(boolean approve) { this.approve = approve; }

        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    }

    public static class MockWebhookRequest {
        private String bookingReference;
        private String eventType = "payment_intent.succeeded";

        public String getBookingReference() { return bookingReference; }
        public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }
}

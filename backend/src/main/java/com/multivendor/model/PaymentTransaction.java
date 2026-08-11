package com.multivendor.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "stripe_session_id", nullable = false, unique = true, length = 150)
    private String stripeSessionId;

    @Column(name = "stripe_payment_intent", length = 150)
    private String stripePaymentIntent;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency = "usd";

    @Column(nullable = false, length = 30)
    private String status; // PENDING, SUCCEEDED, FAILED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PaymentTransaction() {}

    public PaymentTransaction(Booking booking, String stripeSessionId, BigDecimal amount, String currency, String status) {
        this.booking = booking;
        this.stripeSessionId = stripeSessionId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public String getStripeSessionId() { return stripeSessionId; }
    public void setStripeSessionId(String stripeSessionId) { this.stripeSessionId = stripeSessionId; }

    public String getStripePaymentIntent() { return stripePaymentIntent; }
    public void setStripePaymentIntent(String stripePaymentIntent) { this.stripePaymentIntent = stripePaymentIntent; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

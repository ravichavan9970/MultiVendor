package com.multivendor.repository;

import com.multivendor.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByStripeSessionId(String stripeSessionId);
    Optional<PaymentTransaction> findByBookingId(Long bookingId);
}

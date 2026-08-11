package com.multivendor.controller;

import com.multivendor.dto.BookingDtos.MockWebhookRequest;
import com.multivendor.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks/stripe")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final BookingService bookingService;

    @Value("${app.stripe.webhook-secret}")
    private String endpointSecret;

    public StripeWebhookController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        log.info("Received Stripe Webhook Event");
        // Simulated signature check or processing for Stripe Sandbox
        return ResponseEntity.ok("Webhook Received");
    }

    @PostMapping("/simulate")
    public ResponseEntity<?> simulateWebhookEvent(@RequestBody MockWebhookRequest request) {
        if ("payment_intent.succeeded".equalsIgnoreCase(request.getEventType())) {
            return ResponseEntity.ok(bookingService.confirmPaymentAndBooking(request.getBookingReference()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsupported event type for simulation");
    }
}

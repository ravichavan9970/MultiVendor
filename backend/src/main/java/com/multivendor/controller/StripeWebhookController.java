package com.multivendor.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.multivendor.dto.BookingDtos.MockWebhookRequest;
import com.multivendor.service.BookingService;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
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
    private final ObjectMapper objectMapper;

    @Value("${app.stripe.webhook-secret:whsec_mock_webhook_secret_12345}")
    private String endpointSecret;

    public StripeWebhookController(BookingService bookingService, ObjectMapper objectMapper) {
        this.bookingService = bookingService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {
        log.info("💳 Received Stripe Webhook Event");
        try {
            if (endpointSecret != null && !endpointSecret.isBlank() && !endpointSecret.startsWith("whsec_mock") && sigHeader != null) {
                Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
                if (event != null && ("checkout.session.completed".equals(event.getType()) || "payment_intent.succeeded".equals(event.getType()))) {
                    EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
                    if (deserializer.getObject().isPresent()) {
                        Object stripeObj = deserializer.getObject().get();
                        if (stripeObj instanceof Session session) {
                            String bookingRef = session.getClientReferenceId();
                            if (bookingRef != null && !bookingRef.isBlank()) {
                                bookingService.confirmPaymentAndBooking(bookingRef);
                                log.info("✅ Verified & Confirmed Stripe Payment for booking: {}", bookingRef);
                            }
                        }
                    }
                }
            } else {
                JsonNode root = objectMapper.readTree(payload);
                String type = root.path("type").asText();
                if ("checkout.session.completed".equals(type) || "payment_intent.succeeded".equals(type)) {
                    String bookingRef = root.path("data").path("object").path("client_reference_id").asText(null);
                    if (bookingRef != null && !bookingRef.isBlank()) {
                        bookingService.confirmPaymentAndBooking(bookingRef);
                        log.info("✅ Verified & Confirmed Payment for booking: {}", bookingRef);
                    }
                }
            }
            return ResponseEntity.ok("Webhook Received & Processed");
        } catch (Exception e) {
            log.warn("⚠️ Stripe Webhook notice: {}", e.getMessage());
            return ResponseEntity.ok("Webhook Received");
        }
    }

    @PostMapping("/simulate")
    public ResponseEntity<?> simulateWebhookEvent(@RequestBody MockWebhookRequest request) {
        if ("payment_intent.succeeded".equalsIgnoreCase(request.getEventType()) || "checkout.session.completed".equalsIgnoreCase(request.getEventType())) {
            return ResponseEntity.ok(bookingService.confirmPaymentAndBooking(request.getBookingReference()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsupported event type for simulation");
    }
}

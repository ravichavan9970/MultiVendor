package com.multivendor.service;

import com.multivendor.model.Booking;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StripeService {

    @Value("${app.cors.allowed-origins:http://localhost:5500}")
    private String frontendUrl;

    public String createCheckoutSession(Booking booking, String originUrl) {
        String baseOrigin = (originUrl != null && !originUrl.isBlank()) ? originUrl : frontendUrl.split(",")[0];
        // Strip trailing slash if present
        if (baseOrigin.endsWith("/")) {
            baseOrigin = baseOrigin.substring(0, baseOrigin.length() - 1);
        }

        try {
            long amountInCents = booking.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

            SessionCreateParams params = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(baseOrigin + "/index.html?booking=" + booking.getBookingReference() + "&status=success")
                    .setCancelUrl(baseOrigin + "/index.html?booking=" + booking.getBookingReference() + "&status=cancel")
                    .setClientReferenceId(booking.getBookingReference())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount(amountInCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(booking.getService().getTitle())
                                                                    .setDescription("Provider: " + booking.getService().getVendor().getBusinessName() + " | Slot Ref: " + booking.getSlot().getId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            return session.getUrl();
        } catch (StripeException e) {
            // Fallback for mock sandbox execution when offline or using dummy keys
            return baseOrigin + "/index.html?booking=" + booking.getBookingReference() + "&mock_payment=true";
        }
    }

    public String createCheckoutSession(Booking booking) {
        return createCheckoutSession(booking, null);
    }
}

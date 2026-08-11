package com.multivendor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Value("${app.sms.twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${app.sms.twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${app.sms.twilio.from-phone:}")
    private String twilioFromPhone;

    /**
     * Sends real SMS via Twilio REST API if credentials are configured.
     * Falls back to logging if running in sandbox/demo mode.
     */
    public boolean sendOtpSms(String toPhoneNumber, String otpCode) {
        String messageBody = "Your MultiVendor verification code is: " + otpCode + ". Valid for 10 minutes.";

        if (twilioAccountSid != null && !twilioAccountSid.isBlank() &&
            twilioAuthToken != null && !twilioAuthToken.isBlank()) {
            
            try {
                String url = "https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid + "/Messages.json";
                String form = "To=" + java.net.URLEncoder.encode(toPhoneNumber, "UTF-8") +
                              "&From=" + java.net.URLEncoder.encode(twilioFromPhone, "UTF-8") +
                              "&Body=" + java.net.URLEncoder.encode(messageBody, "UTF-8");

                String encoding = java.util.Base64.getEncoder().encodeToString((twilioAccountSid + ":" + twilioAuthToken).getBytes());

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", "Basic " + encoding)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(form))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    log.info("📡 [REAL SMS SENT] Successfully delivered OTP via Twilio to {}", toPhoneNumber);
                    return true;
                } else {
                    log.warn("⚠️ Twilio SMS API returned status {}: {}", response.statusCode(), response.body());
                }
            } catch (Exception e) {
                log.error("❌ Failed to send Twilio SMS to {}: {}", toPhoneNumber, e.getMessage());
            }
        }

        // Sandbox/Demo logging fallback
        log.info("💬 [SANDBOX SMS DEMO] To: {} | Message: {}", toPhoneNumber, messageBody);
        return false;
    }
}

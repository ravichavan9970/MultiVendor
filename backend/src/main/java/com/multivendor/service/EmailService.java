package com.multivendor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void sendOtpEmail(String toEmail, String otpCode) {
        String subject = "Your MultiVendor Account Verification OTP";
        log.info("📧 [OTP EMAIL DISPATCH] To: {} | Subject: {} | Verification Code: [{}]", toEmail, subject, otpCode);
    }
}

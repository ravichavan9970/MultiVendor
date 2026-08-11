package com.multivendor.service;

import com.multivendor.dto.AuthDtos.*;
import com.multivendor.exception.BadRequestException;
import com.multivendor.model.Role;
import com.multivendor.model.User;
import com.multivendor.model.VendorProfile;
import com.multivendor.repository.UserRepository;
import com.multivendor.repository.VendorProfileRepository;
import com.multivendor.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final SmsService smsService;
    private final EmailService emailService;

    // In-memory store for pending OTP registrations (valid for 10 minutes)
    private final Map<String, PendingRegistration> pendingOtps = new ConcurrentHashMap<>();

    private static class PendingRegistration {
        RegisterRequest request;
        String otpCode;
        LocalDateTime expiresAt;

        PendingRegistration(RegisterRequest request, String otpCode, LocalDateTime expiresAt) {
            this.request = request;
            this.otpCode = otpCode;
            this.expiresAt = expiresAt;
        }
    }

    public AuthService(UserRepository userRepository, VendorProfileRepository vendorProfileRepository,
                       PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider,
                       AuthenticationManager authenticationManager,
                       SmsService smsService, EmailService emailService) {
        this.userRepository = userRepository;
        this.vendorProfileRepository = vendorProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.smsService = smsService;
        this.emailService = emailService;
    }

    /**
     * Step 1 of OTP Registration:
     * Validates input, generates 6-digit OTP, stores draft, and dispatches OTP via SMS & Email.
     */
    public OtpInitiateResponse initiateRegistration(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address is already registered: " + request.getEmail());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            String cleanPhone = request.getPhoneNumber().trim();
            if (userRepository.existsByPhoneNumber(cleanPhone)) {
                throw new BadRequestException("Mobile phone number is already registered: " + cleanPhone);
            }
        }

        // Generate 6-digit random OTP code
        String otpCode = String.format("%06d", new Random().nextInt(900000) + 100000);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        pendingOtps.put(request.getEmail().toLowerCase(), new PendingRegistration(request, otpCode, expiresAt));

        // Dispatch SMS & Email
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            smsService.sendOtpSms(request.getPhoneNumber(), otpCode);
        }
        emailService.sendOtpEmail(request.getEmail(), otpCode);

        log.info("📲 [OTP SERVICE] Generated 6-Digit OTP [{}] for Registration. Sent to Email: {} | Mobile: {}",
                otpCode, request.getEmail(), request.getPhoneNumber());

        return new OtpInitiateResponse(
                "OTP verification code sent to your Email & Mobile Number. Please enter the 6-digit code to complete registration.",
                request.getEmail(),
                otpCode
        );
    }

    /**
     * Step 2 of OTP Registration:
     * Verifies the 6-digit OTP code and persists user entity directly to MySQL database.
     */
    @Transactional
    public AuthResponse verifyOtpAndCompleteRegister(VerifyOtpRequest verifyRequest) {
        String key = verifyRequest.getEmail().toLowerCase();
        PendingRegistration pending = pendingOtps.get(key);

        if (pending == null) {
            throw new BadRequestException("No pending OTP registration found for: " + verifyRequest.getEmail());
        }

        if (LocalDateTime.now().isAfter(pending.expiresAt)) {
            pendingOtps.remove(key);
            throw new BadRequestException("OTP code has expired. Please initiate registration again.");
        }

        if (!pending.otpCode.equals(verifyRequest.getOtpCode().trim())) {
            throw new BadRequestException("Invalid 6-digit OTP code. Please check your Email or SMS.");
        }

        RegisterRequest request = pending.request;

        // Persist User entity into MySQL
        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                request.getPhoneNumber(),
                request.getRole() != null ? request.getRole() : Role.VENDOR // Default to VENDOR role for registration
        );

        User savedUser = userRepository.save(user);
        
        // Ensure every vendor user gets a dedicated VendorProfile (reuse existing if present to prevent duplicates)
        VendorProfile savedProfile = vendorProfileRepository.findByUserId(savedUser.getId())
                .orElseGet(() -> {
                    String businessName = request.getBusinessName() != null && !request.getBusinessName().isBlank() ? request.getBusinessName() : savedUser.getFullName() + "'s Academy";
                    String category = request.getCategory() != null ? request.getCategory() : "TUTORING";
                    VendorProfile profile = new VendorProfile(savedUser, businessName, category, "Welcome to my store!", "Remote", BigDecimal.valueOf(85.00));
                    return vendorProfileRepository.save(profile);
                });
        Long vendorId = savedProfile.getId();

        pendingOtps.remove(key);

        // Auto-authenticate and issue JWT
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(savedUser.getEmail(), request.getPassword())
        );

        String jwt = tokenProvider.generateToken(authentication);
        log.info("✅ User {} ({}) successfully verified OTP and created account in MySQL!", savedUser.getFullName(), savedUser.getEmail());

        return new AuthResponse(jwt, savedUser.getId(), savedUser.getEmail(), savedUser.getFullName(), savedUser.getRole(), vendorId);
    }

    /**
     * Direct Registration Fallback (compatibility):
     */
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        OtpInitiateResponse init = initiateRegistration(request);
        VerifyOtpRequest verify = new VerifyOtpRequest();
        verify.setEmail(request.getEmail());
        verify.setOtpCode(init.getDemoOtpCode());
        return verifyOtpAndCompleteRegister(verify);
    }

    /**
     * Dual Identifier Login:
     * Accepts either Email Address OR Mobile Phone Number.
     */
    @Transactional
    public AuthResponse loginUser(LoginRequest request) {
        String identifier = request.getEmail() != null ? request.getEmail().trim() : "";

        // Find user by email or mobile phone number
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhoneNumber(identifier))
                .or(() -> userRepository.findByPhoneNumber("+" + identifier))
                .orElseThrow(() -> new BadRequestException("No account found matching Email or Phone Number: " + identifier));

        // Authenticate with user's actual email & password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        // Get or create dedicated VendorProfile for this user
        VendorProfile profile = vendorProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    String businessName = user.getFullName() + "'s Academy";
                    VendorProfile newProfile = new VendorProfile(user, businessName, "TUTORING", "Verified Provider", "Remote", BigDecimal.valueOf(85.00));
                    return vendorProfileRepository.save(newProfile);
                });

        Long vendorId = profile.getId();

        log.info("🔓 User {} ({}) successfully logged in as Vendor Profile ID {}", user.getFullName(), user.getEmail(), vendorId);

        return new AuthResponse(jwt, user.getId(), user.getEmail(), user.getFullName(), user.getRole(), vendorId);
    }
}

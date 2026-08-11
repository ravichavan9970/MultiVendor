package com.multivendor.controller;

import com.multivendor.dto.AuthDtos.*;
import com.multivendor.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register-initiate")
    public ResponseEntity<OtpInitiateResponse> initiateRegistration(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.initiateRegistration(request));
    }

    @PostMapping("/register-verify-otp")
    public ResponseEntity<AuthResponse> verifyOtpAndRegister(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtpAndCompleteRegister(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.loginUser(request));
    }
}

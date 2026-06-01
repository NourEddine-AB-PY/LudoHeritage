package org.LudoHeritage.auth;

import jakarta.validation.Valid;
import org.LudoHeritage.auth.dto.AuthResponse;
import org.LudoHeritage.auth.dto.LoginRequest;
import org.LudoHeritage.auth.dto.OnboardingRequest;
import org.LudoHeritage.auth.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PutMapping("/users/{userId}/onboarding")
    public ResponseEntity<AuthResponse> completeOnboarding(
            @PathVariable String userId,
            @Valid @RequestBody OnboardingRequest request
    ) {
        return ResponseEntity.ok(authService.saveOnboarding(userId, request));
    }
}

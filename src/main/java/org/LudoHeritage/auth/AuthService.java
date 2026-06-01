package org.LudoHeritage.auth;

import org.LudoHeritage.agent.UserProfile;
import org.LudoHeritage.auth.dto.AuthResponse;
import org.LudoHeritage.auth.dto.LoginRequest;
import org.LudoHeritage.auth.dto.OnboardingRequest;
import org.LudoHeritage.auth.dto.RegisterRequest;
import org.LudoHeritage.auth.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }

        UserDocument user = new UserDocument();
        user.setDisplayName(request.displayName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setOnboardingCompleted(false);
        user.setProfile(new UserProfile());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        UserDocument saved = userRepository.save(user);
        return new AuthResponse("Account created successfully.", true, toUserResponse(saved));
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        UserDocument user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }

        return new AuthResponse("Login successful.", !user.isOnboardingCompleted(), toUserResponse(user));
    }

    public AuthResponse saveOnboarding(String userId, OnboardingRequest request) {
        UserDocument user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        UserProfile profile = user.getProfile() == null ? new UserProfile() : user.getProfile();
        profile.setNiveau(request.niveau().trim());
        profile.setTypeJeuPrefere(request.typeJeuPrefere().trim());
        profile.setRegionPreferee(request.regionPreferee().trim());
        profile.setLangue(request.langue().trim());

        user.setProfile(profile);
        user.setOnboardingCompleted(true);
        user.setUpdatedAt(Instant.now());

        UserDocument saved = userRepository.save(user);
        return new AuthResponse("Onboarding completed.", false, toUserResponse(saved));
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private UserResponse toUserResponse(UserDocument user) {
        return new UserResponse(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.isOnboardingCompleted(),
                user.getProfile()
        );
    }
}

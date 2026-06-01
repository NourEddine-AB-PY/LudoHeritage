package org.LudoHeritage.auth;

import org.LudoHeritage.agent.UserProfile;
import org.LudoHeritage.auth.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder);
    }

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    void register_newUser_requiresOnboardingAndReturnsUserData() {
        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("$hashed$");
        when(userRepository.save(any())).thenReturn(userDoc("1", "Alice", "alice@example.com", false));

        AuthResponse response = authService.register(
                new RegisterRequest("Alice", "alice@example.com", "secret123"));

        assertThat(response.requiresOnboarding()).isTrue();
        assertThat(response.user().email()).isEqualTo("alice@example.com");
        assertThat(response.user().displayName()).isEqualTo("Alice");
        assertThat(response.message()).containsIgnoringCase("created");
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        when(userRepository.findByEmailIgnoreCase("alice@example.com"))
                .thenReturn(Optional.of(userDoc("1", "Alice", "alice@example.com", false)));

        assertThatThrownBy(() ->
                authService.register(new RegisterRequest("Alice", "alice@example.com", "secret123")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void register_mixedCaseEmail_storedLowercase() {
        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(userRepository.save(any())).thenReturn(userDoc("1", "Alice", "alice@example.com", false));

        authService.register(new RegisterRequest("Alice", "ALICE@EXAMPLE.COM", "secret123"));

        ArgumentCaptor<UserDocument> captor = ArgumentCaptor.forClass(UserDocument.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void register_passwordIsHashed() {
        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("$bcrypt$hash$");
        when(userRepository.save(any())).thenReturn(userDoc("1", "Alice", "alice@example.com", false));

        authService.register(new RegisterRequest("Alice", "alice@example.com", "secret123"));

        ArgumentCaptor<UserDocument> captor = ArgumentCaptor.forClass(UserDocument.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash())
                .isEqualTo("$bcrypt$hash$")
                .doesNotContain("secret123");
    }

    // ── login ────────────────────────────────────────────────────────────────

    @Test
    void login_correctCredentials_returnsUser() {
        UserDocument user = userDoc("1", "Alice", "alice@example.com", true);
        user.setPasswordHash("$hashed$");
        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "$hashed$")).thenReturn(true);

        AuthResponse response = authService.login(new LoginRequest("alice@example.com", "secret123"));

        assertThat(response.requiresOnboarding()).isFalse();
        assertThat(response.user().displayName()).isEqualTo("Alice");
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        UserDocument user = userDoc("1", "Alice", "alice@example.com", true);
        user.setPasswordHash("$hashed$");
        when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "$hashed$")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice@example.com", "wrong")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void login_unknownEmail_throwsUnauthorized() {
        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@example.com", "pass")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    // ── onboarding ───────────────────────────────────────────────────────────

    @Test
    void saveOnboarding_validUser_persistsProfileAndClearsFlag() {
        UserDocument user = userDoc("1", "Alice", "alice@example.com", false);
        when(userRepository.findById("1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.saveOnboarding("1",
                new OnboardingRequest("Debutant", "Strategie", "Afrique", "Francais"));

        assertThat(response.requiresOnboarding()).isFalse();
        verify(userRepository).save(argThat(u ->
                u.isOnboardingCompleted()
                && "Strategie".equals(u.getProfile().getTypeJeuPrefere())
                && "Afrique".equals(u.getProfile().getRegionPreferee())));
    }

    @Test
    void saveOnboarding_unknownUser_throwsNotFound() {
        when(userRepository.findById("99")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.saveOnboarding("99",
                new OnboardingRequest("Debutant", "Strategie", "Afrique", "Francais")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private UserDocument userDoc(String id, String name, String email, boolean onboarded) {
        UserDocument u = new UserDocument();
        u.setId(id);
        u.setDisplayName(name);
        u.setEmail(email);
        u.setOnboardingCompleted(onboarded);
        u.setProfile(new UserProfile());
        return u;
    }
}

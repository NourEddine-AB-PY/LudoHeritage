package org.LudoHeritage.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.LudoHeritage.agent.UserProfile;
import org.LudoHeritage.auth.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;

    // ── POST /api/auth/register ───────────────────────────────────────────────

    @Test
    void register_validBody_returns200WithRequiresOnboarding() throws Exception {
        when(authService.register(any()))
                .thenReturn(new AuthResponse("Account created.", true, dummyUser()));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("Alice", "alice@example.com", "secret123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiresOnboarding").value(true))
                .andExpect(jsonPath("$.user.email").value("alice@example.com"));
    }

    @Test
    void register_blankDisplayName_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"\",\"email\":\"alice@example.com\",\"password\":\"secret123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Alice\",\"email\":\"not-an-email\",\"password\":\"secret123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_passwordTooShort_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Alice\",\"email\":\"alice@example.com\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        when(authService.register(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists."));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("Alice", "alice@example.com", "secret123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isString());
    }

    // ── POST /api/auth/login ──────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200() throws Exception {
        when(authService.login(any()))
                .thenReturn(new AuthResponse("Login successful.", false, dummyUser()));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("alice@example.com", "secret123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiresOnboarding").value(false));
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        when(authService.login(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("alice@example.com", "wrongpass"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void login_missingEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"password\":\"secret123\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/auth/users/{id}/onboarding ──────────────────────────────────

    @Test
    void completeOnboarding_validBody_returns200() throws Exception {
        when(authService.saveOnboarding(any(), any()))
                .thenReturn(new AuthResponse("Onboarding completed.", false, dummyUser()));

        mockMvc.perform(put("/api/auth/users/1/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OnboardingRequest("Debutant", "Strategie", "Afrique", "Francais"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requiresOnboarding").value(false));
    }

    @Test
    void completeOnboarding_userNotFound_returns404() throws Exception {
        when(authService.saveOnboarding(any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        mockMvc.perform(put("/api/auth/users/99/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OnboardingRequest("Debutant", "Strategie", "Afrique", "Francais"))))
                .andExpect(status().isNotFound());
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private UserResponse dummyUser() {
        return new UserResponse("1", "Alice", "alice@example.com", false, new UserProfile());
    }
}

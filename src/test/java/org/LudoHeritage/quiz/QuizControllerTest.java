package org.LudoHeritage.quiz;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuizController.class)
class QuizControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean QuizScoreRepository quizScoreRepository;

    // ── POST /api/quiz/scores ─────────────────────────────────────────────────

    @Test
    void submitScore_validPayload_returns200WithSavedScore() throws Exception {
        QuizScore saved = new QuizScore("abc123", "user1", "Alice", 8, 10, Instant.parse("2025-01-15T10:00:00Z"));
        when(quizScoreRepository.save(any())).thenReturn(saved);

        mockMvc.perform(post("/api/quiz/scores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SubmitScoreRequest("user1", "Alice", 8, 10))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(8))
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.displayName").value("Alice"))
                .andExpect(jsonPath("$.id").value("abc123"));
    }

    @Test
    void submitScore_perfectScore_returns200() throws Exception {
        QuizScore saved = new QuizScore("xyz", "user2", "Bob", 10, 10, Instant.now());
        when(quizScoreRepository.save(any())).thenReturn(saved);

        mockMvc.perform(post("/api/quiz/scores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SubmitScoreRequest("user2", "Bob", 10, 10))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(10));
    }

    // ── GET /api/quiz/leaderboard ─────────────────────────────────────────────

    @Test
    void getLeaderboard_returns200WithTop10Scores() throws Exception {
        List<QuizScore> scores = List.of(
                new QuizScore("1", "u1", "Alice", 10, 10, Instant.now()),
                new QuizScore("2", "u2", "Bob",   8, 10, Instant.now()),
                new QuizScore("3", "u3", "Carol", 6, 10, Instant.now())
        );
        when(quizScoreRepository.findTop10ByOrderByScoreDescPlayedAtAsc()).thenReturn(scores);

        mockMvc.perform(get("/api/quiz/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].score").value(10))
                .andExpect(jsonPath("$[0].displayName").value("Alice"));
    }

    @Test
    void getLeaderboard_emptyBoard_returns200WithEmptyArray() throws Exception {
        when(quizScoreRepository.findTop10ByOrderByScoreDescPlayedAtAsc()).thenReturn(List.of());

        mockMvc.perform(get("/api/quiz/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

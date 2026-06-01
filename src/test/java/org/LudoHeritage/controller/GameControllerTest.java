package org.LudoHeritage.controller;

import org.LudoHeritage.model.Game;
import org.LudoHeritage.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean GameService gameService;

    // ── GET /api/jeux ────────────────────────────────────────────────────────

    @Test
    void listGames_noFilter_returns200WithArray() throws Exception {
        when(gameService.getAllGames(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(awale(), go()));

        mockMvc.perform(get("/api/jeux"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Awale"));
    }

    @Test
    void listGames_withRegionParam_filtersOnRegion() throws Exception {
        when(gameService.getAllGames(isNull(), eq("Afrique"), isNull(), isNull()))
                .thenReturn(List.of(awale()));

        mockMvc.perform(get("/api/jeux?region=Afrique"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].region").value("Afrique"));
    }

    @Test
    void listGames_withDifficultyParam_filtersOnDifficulty() throws Exception {
        when(gameService.getAllGames(isNull(), isNull(), isNull(), eq("Debutant")))
                .thenReturn(List.of(awale()));

        mockMvc.perform(get("/api/jeux?difficulty=Debutant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].difficulty").value("Debutant"));
    }

    // ── GET /api/jeux/{id} ────────────────────────────────────────────────────

    @Test
    void getGame_existingId_returns200() throws Exception {
        when(gameService.getGameById(1L)).thenReturn(Optional.of(awale()));

        mockMvc.perform(get("/api/jeux/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Awale"))
                .andExpect(jsonPath("$.playable").value(true));
    }

    @Test
    void getGame_nonExistingId_returns404() throws Exception {
        when(gameService.getGameById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/jeux/999"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/jeux/day ────────────────────────────────────────────────────

    @Test
    void getGameOfTheDay_returns200WithGameObject() throws Exception {
        when(gameService.getGameOfTheDay()).thenReturn(awale());

        mockMvc.perform(get("/api/jeux/day"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Awale"))
                .andExpect(jsonPath("$.id").value(1));
    }

    // ── GET /api/jeux/regions ─────────────────────────────────────────────────

    @Test
    void getRegions_returns200WithStringArray() throws Exception {
        when(gameService.getRegions()).thenReturn(List.of("Afrique", "Asie", "Europe"));

        mockMvc.perform(get("/api/jeux/regions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("Afrique"));
    }

    // ── GET /api/jeux/periods ─────────────────────────────────────────────────

    @Test
    void getPeriods_returns200WithStringArray() throws Exception {
        when(gameService.getPeriods()).thenReturn(List.of("Ancient", "Medieval", "Modern"));

        mockMvc.perform(get("/api/jeux/periods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    // ── GET /api/jeux/categories ──────────────────────────────────────────────

    @Test
    void getCategories_returns200WithStringArray() throws Exception {
        when(gameService.getCategories()).thenReturn(List.of("Board", "Sow", "War"));

        mockMvc.perform(get("/api/jeux/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Board"));
    }

    // ── GET /api/jeux/search ──────────────────────────────────────────────────

    @Test
    void searchGames_withQueryParam_returns200() throws Exception {
        when(gameService.searchGames(eq("go"), isNull(), isNull())).thenReturn(List.of(go()));

        mockMvc.perform(get("/api/jeux/search?q=go"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Go"));
    }

    // ── fixtures ──────────────────────────────────────────────────────────────

    private Game awale() {
        return new Game(1L, "Awale", "Afrique", "Afrique de l'Ouest",
                "Strategie", "Debutant", "Jeu de semailles.", "Regles.", "Histoire.", "");
    }

    private Game go() {
        return new Game(4L, "Go", "Asie", "Chine",
                "Strategie", "Passionne", "Jeu de territoire.", "Regles.", "Histoire.", "");
    }
}

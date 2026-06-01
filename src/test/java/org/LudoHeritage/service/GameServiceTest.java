package org.LudoHeritage.service;

import org.LudoHeritage.model.Game;
import org.LudoHeritage.model.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock GameRepository gameRepository;

    GameService gameService;

    @BeforeEach
    void setUp() {
        // Return empty list so GameService uses its in-memory fallback catalog.
        when(gameRepository.findAll()).thenReturn(Collections.emptyList());
        gameService = new GameService(gameRepository);
    }

    // ── getAllGames ───────────────────────────────────────────────────────────

    @Test
    void getAllGames_noFilter_returns20Games() {
        List<Game> games = gameService.getAllGames(null, null, null, null);
        assertThat(games).hasSize(20);
    }

    @Test
    void getAllGames_filterAfrique_returnsOnlyAfricanGames() {
        List<Game> games = gameService.getAllGames(null, "Afrique", null, null);
        assertThat(games).isNotEmpty()
                .allMatch(g -> g.region().equalsIgnoreCase("Afrique"));
    }

    @Test
    void getAllGames_filterAsie_returnsOnlyAsianGames() {
        List<Game> games = gameService.getAllGames(null, "Asie", null, null);
        assertThat(games).isNotEmpty()
                .allMatch(g -> g.region().equalsIgnoreCase("Asie"));
    }

    @Test
    void getAllGames_queryByName_awaleFound() {
        List<Game> games = gameService.getAllGames("Awale", null, null, null);
        assertThat(games).extracting(Game::name).contains("Awale");
    }

    @Test
    void getAllGames_queryNonExistent_returnsEmpty() {
        List<Game> games = gameService.getAllGames("XYZNONEXISTENT", null, null, null);
        assertThat(games).isEmpty();
    }

    @Test
    void getAllGames_filterByDifficulty_returnsOnlyMatching() {
        List<Game> games = gameService.getAllGames(null, null, null, "Debutant");
        assertThat(games).isNotEmpty()
                .allMatch(g -> g.difficulty().equalsIgnoreCase("Debutant"));
    }

    @Test
    void getAllGames_resultsSortedAlphabetically() {
        List<Game> games = gameService.getAllGames(null, null, null, null);
        List<String> names = games.stream().map(Game::name).toList();
        List<String> sorted = names.stream().sorted().toList();
        assertThat(names).isEqualTo(sorted);
    }

    // ── getGameById ───────────────────────────────────────────────────────────

    @Test
    void getGameById_id1_returnsAwale() {
        Optional<Game> game = gameService.getGameById(1L);
        assertThat(game).isPresent()
                .get().extracting(Game::name).isEqualTo("Awale");
    }

    @Test
    void getGameById_id1_isPlayable() {
        Game game = gameService.getGameById(1L).orElseThrow();
        assertThat(game.playable()).isTrue();
    }

    @Test
    void getGameById_unknownId_returnsEmpty() {
        Optional<Game> game = gameService.getGameById(999L);
        assertThat(game).isEmpty();
    }

    // ── metadata queries ──────────────────────────────────────────────────────

    @Test
    void getRegions_returnsDistinctSortedRegions() {
        List<String> regions = gameService.getRegions();
        assertThat(regions).containsExactlyInAnyOrder("Afrique", "Asie", "Europe");
    }

    @Test
    void getPeriods_returnsDistinctPeriods() {
        List<String> periods = gameService.getPeriods();
        assertThat(periods).containsExactlyInAnyOrder("Ancient", "Medieval", "Modern");
    }

    @Test
    void getCategories_returnsNonEmptyList() {
        List<String> cats = gameService.getCategories();
        assertThat(cats).isNotEmpty().contains("Board");
    }

    // ── getGameOfTheDay ───────────────────────────────────────────────────────

    @Test
    void getGameOfTheDay_returnsDeterministicResultSameDay() {
        Game first = gameService.getGameOfTheDay();
        Game second = gameService.getGameOfTheDay();
        assertThat(first.id()).isEqualTo(second.id());
    }

    @Test
    void getGameOfTheDay_idIsWithinCatalogRange() {
        Game game = gameService.getGameOfTheDay();
        assertThat(game.id()).isBetween(1L, 20L);
    }

    // ── findSimilarGames ──────────────────────────────────────────────────────

    @Test
    void findSimilarGames_awale_returnsUpTo3DifferentGames() {
        Game awale = gameService.getGameById(1L).orElseThrow();
        List<Game> similar = gameService.findSimilarGames(awale);
        assertThat(similar).hasSizeLessThanOrEqualTo(3)
                .noneMatch(g -> g.id().equals(awale.id()));
    }

    @Test
    void searchGames_byQuery_delegatesToGetAllGames() {
        List<Game> result = gameService.searchGames("Awale", null, null);
        assertThat(result).extracting(Game::name).contains("Awale");
    }
}

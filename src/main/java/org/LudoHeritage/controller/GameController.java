package org.LudoHeritage.controller;

import org.LudoHeritage.model.Game;
import org.LudoHeritage.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jeux")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    public ResponseEntity<List<Game>> listGames(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String category
    ) {
        String typeOrCategory = category == null || category.isBlank() ? type : category;
        return ResponseEntity.ok(gameService.getAllGames(q, region, typeOrCategory, difficulty));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Game> getGame(@PathVariable Long id) {
        return gameService.getGameById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Game>> searchGames(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String type
    ) {
        return ResponseEntity.ok(gameService.searchGames(q, region, type));
    }

    @GetMapping("/regions")
    public ResponseEntity<List<String>> getRegions() {
        return ResponseEntity.ok(gameService.getRegions());
    }

    @GetMapping("/periods")
    public ResponseEntity<List<String>> getPeriods() {
        return ResponseEntity.ok(gameService.getPeriods());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(gameService.getCategories());
    }

    @GetMapping("/day")
    public ResponseEntity<Game> getGameOfTheDay() {
        return ResponseEntity.ok(gameService.getGameOfTheDay());
    }
}

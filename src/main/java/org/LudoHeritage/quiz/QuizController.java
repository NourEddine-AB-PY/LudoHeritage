package org.LudoHeritage.quiz;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizScoreRepository repository;

    public QuizController(QuizScoreRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/scores")
    public ResponseEntity<QuizScore> submitScore(@RequestBody SubmitScoreRequest request) {
        QuizScore score = new QuizScore(null, request.userId(), request.displayName(),
                request.score(), request.total(), Instant.now());
        return ResponseEntity.ok(repository.save(score));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<QuizScore>> getLeaderboard() {
        return ResponseEntity.ok(repository.findTop10ByOrderByScoreDescPlayedAtAsc());
    }
}

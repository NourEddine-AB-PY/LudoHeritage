package org.LudoHeritage.quiz;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("quiz_scores")
public record QuizScore(
        @Id String id,
        String userId,
        String displayName,
        int score,
        int total,
        Instant playedAt
) {}

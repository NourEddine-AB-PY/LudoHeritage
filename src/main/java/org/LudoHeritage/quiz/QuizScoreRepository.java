package org.LudoHeritage.quiz;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface QuizScoreRepository extends MongoRepository<QuizScore, String> {
    List<QuizScore> findTop10ByOrderByScoreDescPlayedAtAsc();
}

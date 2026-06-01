package org.LudoHeritage.community;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommunityPostRepository extends MongoRepository<CommunityPostDocument, String> {
}

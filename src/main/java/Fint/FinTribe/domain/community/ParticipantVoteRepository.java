package Fint.FinTribe.domain.community;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ParticipantVoteRepository extends MongoRepository<ParticipantVote, ObjectId> {
    @Query("{$and:[{'voteId': ?0}, {'userId': ?1}]}")
    Optional<ParticipantVote> findByVoteIdAndUserId(ObjectId voteId, ObjectId userId);
    @Query("{'voteId': ?0}")
    List<ParticipantVote> findByVoteId(ObjectId voteId);
}

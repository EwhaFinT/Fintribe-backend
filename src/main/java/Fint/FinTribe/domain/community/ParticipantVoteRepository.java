package Fint.FinTribe.domain.community;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ParticipantVoteRepository extends MongoRepository<ParticipantVote, ObjectId> {
    @Query("{'voteId': ?0}")
    List<ParticipantVote> findByVoteId(ObjectId voteId);
}

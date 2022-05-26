package Fint.FinTribe.domain.community;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoteRepository extends MongoRepository<Vote, ObjectId> {
    @Query("{'endTime': ?0}")
    List<Vote> findVotesByEndTime(LocalDateTime endTime);
    @Query("{'communityId': ?0}")
    Optional<Vote> findVoteByCommunityId(ObjectId communityId);
}

package Fint.FinTribe.domain.community;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface VoteRepository extends MongoRepository<Vote, ObjectId> {
    @Query("{'endTime': ?0}")
    List<Vote> findVotesByEndTime(LocalDateTime endTime);
}

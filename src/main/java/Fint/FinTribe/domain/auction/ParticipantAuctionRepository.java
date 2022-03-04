package Fint.FinTribe.domain.auction;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantAuctionRepository extends MongoRepository<ParticipantAuction, ObjectId> {
    @Query("{'userId': ?0}")
    Optional<ParticipantAuction> findByUserId(ObjectId userId);
}
package Fint.FinTribe.domain.auction;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantAuctionRepository extends MongoRepository<ParticipantAuction, Object> {
    @Query("{'userId': ?0}")
    Optional<ParticipantAuction> findByUserId(Object userId);
}
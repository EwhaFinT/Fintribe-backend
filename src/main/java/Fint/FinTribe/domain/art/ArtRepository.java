package Fint.FinTribe.domain.art;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtRepository extends MongoRepository<Art, ObjectId> {
    @Query("{'auctionId': ?0}")
    Optional<Art> findByAuctionId(Object auctionId);
}
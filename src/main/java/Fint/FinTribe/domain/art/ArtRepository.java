package Fint.FinTribe.domain.art;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtRepository extends MongoRepository<Art, Object> {
    @Query("{'auctionId': ?0}")
    Optional<Art> findByAuctionId(Object auctionId);
}
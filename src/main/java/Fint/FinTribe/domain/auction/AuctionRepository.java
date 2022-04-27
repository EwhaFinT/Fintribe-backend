package Fint.FinTribe.domain.auction;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends MongoRepository<Auction, ObjectId> {
    @Query("{'artId': ?0}")
    Optional<Auction> findByArtId(ObjectId artId);

    @Query("{'isDeleted': ?0}")
    List<Auction> findByIsDeleted(boolean isDeleted);
}
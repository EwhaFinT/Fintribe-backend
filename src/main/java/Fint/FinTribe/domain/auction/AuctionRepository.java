package Fint.FinTribe.domain.auction;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionRepository extends MongoRepository<Auction, ObjectId> {
    @Query("{'isDeleted': ?0}")
    List<Auction> findByIsDeleted(boolean isDeleted);
}
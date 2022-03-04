package Fint.FinTribe.domain.auction;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceRepository extends MongoRepository<Price, ObjectId> {
    @Query("{'auctionId': ?0}")
    List<Price> findByAuctionId(ObjectId auctionId);
}
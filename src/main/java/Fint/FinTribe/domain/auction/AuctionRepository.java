package Fint.FinTribe.domain.auction;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends MongoRepository<Auction, ObjectId> {
}
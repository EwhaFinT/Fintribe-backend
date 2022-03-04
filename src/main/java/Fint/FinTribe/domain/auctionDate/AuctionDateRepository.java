package Fint.FinTribe.domain.auctionDate;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AuctionDateRepository extends MongoRepository<AuctionDate, ObjectId> {
    //Optional<AuctionDate> findByAuctionDate(LocalDateTime auctionDate);
}

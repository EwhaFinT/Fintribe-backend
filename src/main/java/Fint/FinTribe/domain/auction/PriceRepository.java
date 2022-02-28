package Fint.FinTribe.domain.auction;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceRepository extends MongoRepository<Price, Object> {
    @Query("{'auctionId': ?0}")
    List<Price> findByAuctionId(Object auctionId);
}
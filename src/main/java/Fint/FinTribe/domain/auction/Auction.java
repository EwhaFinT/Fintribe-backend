package Fint.FinTribe.domain.auction;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@Document(collection = "auction")
public class Auction {
    @Id
    private ObjectId auctionId;
    private ObjectId artId;
    private boolean isDeleted;
}
package Fint.FinTribe.domain.auction;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@Document(collection = "price")
public class Price {
    @Id
    private ObjectId priceId;
    private ObjectId auctionId;
    private double auctionPrice;
    private double remainderRatio;
}
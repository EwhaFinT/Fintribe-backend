package Fint.FinTribe.domain.auction;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@Document(collection = "price")
public class Price {
    @Id
    private Object priceId;
    private Object auctionId;
    private double auctionPrice;
    private double remainderRatio;
}
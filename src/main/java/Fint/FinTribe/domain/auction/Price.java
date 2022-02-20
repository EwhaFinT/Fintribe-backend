package Fint.FinTribe.domain.auction;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@Document(collection = "price")
public class Price {
    private Long priceId;
    private Long auctionPrice;
    private Double remainderRatio;
}

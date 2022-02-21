package Fint.FinTribe.domain.auction;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@Document(collection = "auction")
public class Auction {
    @Id
    private Object auctionId;
    private Object artId;
}
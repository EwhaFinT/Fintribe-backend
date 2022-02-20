package Fint.FinTribe.domain.auction;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@Document(collection = "auction")
public class Auction {
    private Long auctionId;
    private Long artId;
}
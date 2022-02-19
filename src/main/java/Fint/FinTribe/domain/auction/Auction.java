package Fint.FinTribe.domain.auction;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Builder
@Document(collection = "auction")
public class Auction {
    private Long auctionId;
    private Long artId;
    private List<Price> price;
}
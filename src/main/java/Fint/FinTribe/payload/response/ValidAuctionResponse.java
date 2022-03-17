package Fint.FinTribe.payload.response;

import Fint.FinTribe.domain.auction.Auction;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
public class ValidAuctionResponse {
    private List<Auction> auctions;
}

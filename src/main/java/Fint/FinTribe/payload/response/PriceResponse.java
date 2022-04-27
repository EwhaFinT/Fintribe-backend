package Fint.FinTribe.payload.response;

import lombok.*;

@Getter
@AllArgsConstructor
public class PriceResponse {
    private String priceId;
    private String auctionId;
    private double auctionPrice;
    private double remainderRatio;
}

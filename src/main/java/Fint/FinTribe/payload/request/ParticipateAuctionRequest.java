package Fint.FinTribe.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class ParticipateAuctionRequest {
    @NonNull
    private Object userId;
    @NonNull
    private Object priceId;
    @NonNull
    private Object auctionId;
    @NonNull
    private double ratio;
}

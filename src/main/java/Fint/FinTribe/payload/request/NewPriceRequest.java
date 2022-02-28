package Fint.FinTribe.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class NewPriceRequest {
    @NonNull
    private Object userId;
    @NonNull
    private Object auctionId;
    @NonNull
    private double auctionPrice;
    @NonNull
    private double ratio;
}

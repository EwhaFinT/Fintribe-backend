package Fint.FinTribe.payload.request;

import lombok.*;

@Getter
@AllArgsConstructor
public class NewPriceRequest {
    @NonNull
    private String userId;
    @NonNull
    private String artId;
    @NonNull
    private double auctionPrice;
    @NonNull
    private double ratio;
}
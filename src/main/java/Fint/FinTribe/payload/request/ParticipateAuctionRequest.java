package Fint.FinTribe.payload.request;

import lombok.*;

@Getter
@AllArgsConstructor
public class ParticipateAuctionRequest {
    @NonNull
    private String userId;
    @NonNull
    private String priceId;
    @NonNull
    private double ratio;
}

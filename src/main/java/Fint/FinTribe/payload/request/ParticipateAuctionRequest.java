package Fint.FinTribe.payload.request;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
public class ParticipateAuctionRequest {
    @NonNull
    private String userId;
    @NonNull
    private String priceId;
    @NonNull
    private double ratio;
    @NonNull
    private List<String> rlp;
}

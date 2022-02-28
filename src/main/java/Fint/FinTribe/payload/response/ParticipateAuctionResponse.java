package Fint.FinTribe.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class ParticipateAuctionResponse {
    @NonNull
    private Object priceId;
}
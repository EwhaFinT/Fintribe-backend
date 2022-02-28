package Fint.FinTribe.payload.request;

import lombok.*;

@Getter
@AllArgsConstructor
public class PricelistRequest {
    @NonNull
    private Object auctionId;
}

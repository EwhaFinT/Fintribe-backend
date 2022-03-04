package Fint.FinTribe.payload.request;

import lombok.*;
import org.bson.types.ObjectId;

@Getter
@AllArgsConstructor
public class PricelistRequest {
    @NonNull
    private ObjectId auctionId;
}

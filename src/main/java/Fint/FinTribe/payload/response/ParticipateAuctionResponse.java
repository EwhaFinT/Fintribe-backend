package Fint.FinTribe.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bson.types.ObjectId;

@Getter
@AllArgsConstructor
public class ParticipateAuctionResponse {
    @NonNull
    private ObjectId priceId;
}
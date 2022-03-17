package Fint.FinTribe.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bson.types.ObjectId;

@Getter
@AllArgsConstructor
public class NewPriceRequest {
    @NonNull
    private ObjectId userId;
    @NonNull
    private ObjectId auctionId;
    @NonNull
    private double auctionPrice;
    @NonNull
    private double ratio;
    @NonNull
    private String rlp;
}

package Fint.FinTribe.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@AllArgsConstructor
public class ParticipateAuctionRequest {
    @NonNull
    private String userId;
    @NonNull
    private String priceId;
    @NonNull
    private String auctionId;
    @NonNull
    private double ratio;
    @NonNull
    private List<String> rlp;
}

package Fint.FinTribe.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

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
    @NonNull
    private List<String> rlp;
}
package Fint.FinTribe.payload.response;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
public class PricelistResponse {
    @NonNull
    private double price;
    private List<PriceResponse> pricelist;
}

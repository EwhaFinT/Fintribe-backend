package Fint.FinTribe.payload.response;

import Fint.FinTribe.domain.auction.Price;
import lombok.AllArgsConstructor;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
public class PricelistResponse {
    @NonNull
    private double price;
    private List<Price> pricelist;
}

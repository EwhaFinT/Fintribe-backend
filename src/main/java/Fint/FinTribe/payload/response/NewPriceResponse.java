package Fint.FinTribe.payload.response;

import lombok.*;

@Getter
@AllArgsConstructor
public class NewPriceResponse {
    private String priceId;
    private String errorMsg;
}

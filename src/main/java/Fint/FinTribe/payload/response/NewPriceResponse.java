package Fint.FinTribe.payload.response;

import lombok.*;
import org.bson.types.ObjectId;

@Getter
@AllArgsConstructor
public class NewPriceResponse {
    @NonNull
    private ObjectId priceId;
}

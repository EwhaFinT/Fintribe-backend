package Fint.FinTribe.payload.request;

import lombok.*;
import org.bson.types.ObjectId;

@Getter
@AllArgsConstructor
public class RegisterWalletRequest {
    @NonNull
    private ObjectId userId;
    @NonNull
    private String wallet;
}
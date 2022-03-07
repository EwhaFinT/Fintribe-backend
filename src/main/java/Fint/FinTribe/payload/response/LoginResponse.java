package Fint.FinTribe.payload.response;

import lombok.*;
import org.bson.types.ObjectId;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private ObjectId userId;
    @NonNull
    private String message;
}

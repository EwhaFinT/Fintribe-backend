package Fint.FinTribe.payload.response;

import lombok.*;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private ObjectId userId;
    @NonNull
    private String message;
}

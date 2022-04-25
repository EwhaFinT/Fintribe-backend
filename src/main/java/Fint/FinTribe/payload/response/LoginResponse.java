package Fint.FinTribe.payload.response;

import lombok.*;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String userId;
    @NonNull
    private String message;
}

package Fint.FinTribe.payload.response;

import lombok.*;

@Getter
@AllArgsConstructor
public class LoginResponse {
    @NonNull
    private Object userId;
    @NonNull
    private String message;
}

package Fint.FinTribe.payload.request;

import lombok.*;

@Getter
@AllArgsConstructor
public class LoginRequest {
    @NonNull
    private String identity;
    @NonNull
    private String password;
}
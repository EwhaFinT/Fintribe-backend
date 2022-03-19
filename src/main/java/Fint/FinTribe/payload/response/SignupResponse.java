package Fint.FinTribe.payload.response;

import lombok.*;

@Getter
@AllArgsConstructor
public class SignupResponse {
    @NonNull
    private int signupSuccess;
}
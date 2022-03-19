package Fint.FinTribe.payload.request;

import lombok.*;

@Getter
@AllArgsConstructor
public class SignupRequest {
    @NonNull
    private String identity;
    @NonNull
    private String password;
    @NonNull
    private String name;
    @NonNull
    private String phone;
    @NonNull
    private String email;
}

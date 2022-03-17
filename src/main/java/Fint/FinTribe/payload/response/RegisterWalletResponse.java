package Fint.FinTribe.payload.response;

import lombok.*;

@Getter
@AllArgsConstructor
public class RegisterWalletResponse {
    @NonNull
    private int registerSuccess;
}

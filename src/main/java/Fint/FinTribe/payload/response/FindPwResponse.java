package Fint.FinTribe.payload.response;

import lombok.*;

@Getter
@AllArgsConstructor
public class FindPwResponse {
    @NonNull
    private boolean emailSuccess;
}
package Fint.FinTribe.payload.request;

import lombok.*;

@Getter
@AllArgsConstructor
public class FindPwRequest {
    @NonNull
    private String identity;
    @NonNull
    private String email;
}

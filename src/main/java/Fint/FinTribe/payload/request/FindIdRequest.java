package Fint.FinTribe.payload.request;

import lombok.*;

@Getter
@AllArgsConstructor
public class FindIdRequest {
    @NonNull
    private String name;
    @NonNull
    private String phone;
}

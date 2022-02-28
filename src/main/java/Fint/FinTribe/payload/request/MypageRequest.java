package Fint.FinTribe.payload.request;

import lombok.AllArgsConstructor;
import lombok.*;

@Getter
@AllArgsConstructor
public class MypageRequest {
    @NonNull
    private Object userId;
}

package Fint.FinTribe.payload.request;

import lombok.AllArgsConstructor;
import lombok.*;
import org.bson.types.ObjectId;

@Getter
@AllArgsConstructor
public class MypageRequest {
    @NonNull
    private ObjectId userId;
}

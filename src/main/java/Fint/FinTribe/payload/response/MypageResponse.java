package Fint.FinTribe.payload.response;

import lombok.AllArgsConstructor;
import lombok.*;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@AllArgsConstructor
public class MypageResponse {
    private String wallet;
    private List<ObjectId> artIdList;
}

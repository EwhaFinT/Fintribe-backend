package Fint.FinTribe.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.List;

@Getter
@AllArgsConstructor
public class CommunitiesResponse {
    List<String> communityIdList;
}

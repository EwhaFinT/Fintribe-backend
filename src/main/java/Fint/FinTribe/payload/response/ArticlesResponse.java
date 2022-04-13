package Fint.FinTribe.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.Map;

@AllArgsConstructor
@Getter
public class ArticlesResponse {
    private Map<ObjectId, String> articles;
}

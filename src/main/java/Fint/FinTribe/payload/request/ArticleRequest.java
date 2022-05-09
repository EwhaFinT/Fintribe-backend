package Fint.FinTribe.payload.request;

import lombok.Getter;
import org.bson.types.ObjectId;
import javax.validation.constraints.NotNull;

@Getter
public class ArticleRequest {
    @NotNull
    String userId;
    @NotNull
    String title;
    @NotNull
    String content;
    @NotNull
    ObjectId communityId;
    @NotNull
    String identity;
}

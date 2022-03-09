package Fint.FinTribe.payload.request;

import lombok.Getter;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

@Getter
public class ReviseArticleRequest {
    @NotNull
    ObjectId articleId;
    @NotNull
    String title;
    @NotNull
    String content;
}

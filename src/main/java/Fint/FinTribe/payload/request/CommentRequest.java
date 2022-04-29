package Fint.FinTribe.payload.request;

import lombok.Getter;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

@Getter
public class CommentRequest {
    @NotNull
    String userId;
    @NotNull
    String articleId;
    @NotNull
    String tagUser;
    @NotNull
    String content;
    @NotNull
    Integer tagCommentId;
}
package Fint.FinTribe.payload.request;

import lombok.Getter;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

@Getter
public class ReviseCommentRequest {
    @NotNull
    String articleId;
    @NotNull
    Integer commentId;
    @NotNull
    String tagUser;
    @NotNull
    String content;
    @NotNull
    Integer tagCommentId;
}

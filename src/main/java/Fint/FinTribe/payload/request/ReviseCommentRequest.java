package Fint.FinTribe.payload.request;

import lombok.Getter;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

@Getter
public class ReviseCommentRequest {
    @NotNull
    ObjectId articleId;
    @NotNull
    Integer commentId;
    @NotNull
    ObjectId tagUser;
    @NotNull
    String content;
    @NotNull
    ObjectId communityId;
    @NotNull
    Integer tagCommentId;
}

package Fint.FinTribe.payload.request;

import lombok.Getter;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

@Getter
public class CommentRequest {
    @NotNull
    ObjectId userId;
    @NotNull
    ObjectId articleId;
    @NotNull
    ObjectId tagUser;
    @NotNull
    String content;
    @NotNull
    Integer tagCommentId;
}
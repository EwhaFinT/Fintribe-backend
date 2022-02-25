package Fint.FinTribe.payload.request;

import lombok.Getter;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
public class CommentRequest {
    @NotNull
    ObjectId userId;
    @NotNull
    Long articleId;
    @NotNull
    ObjectId tagUser;
    @NotNull
    String content;
    @NotNull
    ObjectId communityId;
    @NotNull
    List<Integer> tagCommentId;
}

package Fint.FinTribe.payload.request;

import lombok.Getter;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
public class ReviseCommentRequest {
    @NotNull
    Long articleId;
    @NotNull
    List<Integer> commentId;
    @NotNull
    ObjectId tagUser;
    @NotNull
    String content;
    @NotNull
    ObjectId communityId;
    @NotNull
    List<Integer> tagCommetId;
}

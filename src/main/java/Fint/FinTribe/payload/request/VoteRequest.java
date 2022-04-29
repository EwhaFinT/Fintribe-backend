package Fint.FinTribe.payload.request;

import lombok.Getter;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

@Getter
public class VoteRequest {
    @NotNull
    String userId;
    @NotNull
    String voteId;
    @NotNull
    Boolean choice;
}

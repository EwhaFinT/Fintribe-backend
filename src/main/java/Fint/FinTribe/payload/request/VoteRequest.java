package Fint.FinTribe.payload.request;

import lombok.Getter;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

@Getter
public class VoteRequest {
    @NotNull
    ObjectId userId;
    @NotNull
    ObjectId voteId;
    @NotNull
    Boolean choice;
}

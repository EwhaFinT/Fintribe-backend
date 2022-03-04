package Fint.FinTribe.payload.request;

import lombok.Getter;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;

@Getter
public class VoteRequest {
    @NotNull
    ObjectId userId;
    @NotNull
    ObjectId communityId;
    @NotNull
    Integer voteId;
    @NotNull
    Boolean choice;
}

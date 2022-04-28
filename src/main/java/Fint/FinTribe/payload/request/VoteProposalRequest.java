package Fint.FinTribe.payload.request;

import lombok.Getter;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
public class VoteProposalRequest {
    @NotNull
    String communityId;
    @NotNull
    String userId;
    @NotNull
    String title;
    @NotNull
    Double resalePrice;
    @NotNull
    LocalDateTime endTime;
}

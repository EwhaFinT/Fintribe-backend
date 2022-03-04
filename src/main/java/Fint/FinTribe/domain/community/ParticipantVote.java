package Fint.FinTribe.domain.community;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Builder
@Document(collection = "participant_vote")
public class ParticipantVote {
    private ObjectId userId;
    private Boolean choice;
    private LocalDateTime participatedAt;
    private Float ratio;
}

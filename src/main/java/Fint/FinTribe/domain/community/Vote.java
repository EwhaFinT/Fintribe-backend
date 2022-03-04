package Fint.FinTribe.domain.community;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Document(collection = "vote")
public class Vote {
    private Integer voteId;
    private ObjectId userId;
    private String identity;
    private String title;
    private Double resalePrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isDeleted;
    private Double agreement;
    private Double disagreement;
    private List<ParticipantVote> participants;
}

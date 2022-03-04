package Fint.FinTribe.domain.community;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Document(collection = "vote")
public class Vote {
    private ObjectId userId;
    private String title;
    private Double resalePrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isDeleted;
    private Float agreement;
    private Float disagreement;
    private List<ParticipantVote> participants;
}

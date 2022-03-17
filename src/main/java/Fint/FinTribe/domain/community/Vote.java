package Fint.FinTribe.domain.community;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Document(collection = "vote")
public class Vote {
    @Id
    private ObjectId voteId;
    private ObjectId communityId;
    private ObjectId userId;
    private String identity;
    private String title;
    private Double resalePrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isDeleted;
    private Double agreement;
    private Double disagreement;
}

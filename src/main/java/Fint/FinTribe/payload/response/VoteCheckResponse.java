package Fint.FinTribe.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class VoteCheckResponse {
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
}

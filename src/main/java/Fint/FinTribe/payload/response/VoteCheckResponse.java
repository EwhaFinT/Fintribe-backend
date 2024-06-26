package Fint.FinTribe.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class VoteCheckResponse {
    private String voteId;
    private String userId;
    private String identity;
    private String title;
    private Double resalePrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isDeleted;
    private Double agreement;
    private Double disagreement;
    private Double ratio;
    private List<String> voteParticipants;
    public VoteCheckResponse(String msg){
        voteId = msg;
    }
}

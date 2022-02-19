package Fint.FinTribe.domain.auction;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@Document(collection = "participant_auction")
public class ParticipantAuction {
    private Long userId;
    private Double ratio;
}
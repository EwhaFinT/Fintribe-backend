package Fint.FinTribe.domain.auction;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@Document(collection = "participant_auction")
public class ParticipantAuction {
    @Id
    private Object participantAuctionId;
    private Object priceId;
    private Object userId;
    private double ratio;
}
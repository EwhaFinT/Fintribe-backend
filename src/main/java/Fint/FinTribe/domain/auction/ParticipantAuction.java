package Fint.FinTribe.domain.auction;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@Document(collection = "participant_auction")
public class ParticipantAuction {
    @Id
    private ObjectId participantAuctionId;
    private ObjectId priceId;
    private ObjectId userId;
    private double ratio;
}
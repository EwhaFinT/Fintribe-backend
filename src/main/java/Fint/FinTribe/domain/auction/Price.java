package Fint.FinTribe.domain.auction;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Builder
@Document(collection = "price")
public class Price {
    private Long priceId;
    private Long auctionPrice;
    private Double remainderRatio;
    private List<ParticipantAuction> participantAuction;
}

package Fint.FinTribe.domain.auctionDate;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Document(collection = "auction_date")
public class AuctionDate {
    @Id
    private ObjectId auctionDateId;
    private LocalDateTime auctionDate;
    private List<ObjectId> artId;
}
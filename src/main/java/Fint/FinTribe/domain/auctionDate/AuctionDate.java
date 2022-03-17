package Fint.FinTribe.domain.auctionDate;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@Document(collection = "auction_date")
public class AuctionDate {
    @Id
    private ObjectId auctionDateId;
    private LocalDate auctionDate;
    private List<ObjectId> artId;
}
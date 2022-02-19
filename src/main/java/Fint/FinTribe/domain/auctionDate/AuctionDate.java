package Fint.FinTribe.domain.auctionDate;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Document(collection = "auction_date")
public class AuctionDate {
    private Long auctionDateId;
    private LocalDateTime auctionDate;
    private List<Long> artId;
}
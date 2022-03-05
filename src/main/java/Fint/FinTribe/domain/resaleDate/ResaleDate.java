package Fint.FinTribe.domain.resaleDate;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Document(collection = "resale_date")
public class ResaleDate {
    @Id
    private ObjectId resaleDateId;
    private LocalDateTime resaleDate;
    private List<ObjectId> artId;
}

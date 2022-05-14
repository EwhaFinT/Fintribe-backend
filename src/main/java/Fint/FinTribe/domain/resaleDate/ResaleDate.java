package Fint.FinTribe.domain.resaleDate;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@Document(collection = "resale_date")
public class ResaleDate {
    @Id
    private ObjectId resaleDateId;
    private LocalDate resaleDate;
    private List<ObjectId> artId;
}

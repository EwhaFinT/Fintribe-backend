package Fint.FinTribe.domain.art;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@Document(collection = "art")
public class Art {
    @Id
    private ObjectId artId;
    private String painter;
    private String artName;
    private String detail;
    private double price;
    private String nftAdd;
    private String paint;
    private boolean sold;
    private List<ObjectId> userId;
    private List<Double> ratio;
}
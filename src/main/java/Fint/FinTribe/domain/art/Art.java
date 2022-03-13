package Fint.FinTribe.domain.art;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
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
    private Boolean sold = false;
    private String wallet;
    private List<ObjectId> userId;
}
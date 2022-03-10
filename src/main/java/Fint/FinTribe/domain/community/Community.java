package Fint.FinTribe.domain.community;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Builder
@Document(collection = "community")
public class Community {
    @Id
    private ObjectId communityId;
    private ObjectId artId;
    private Boolean isDeleted;
}

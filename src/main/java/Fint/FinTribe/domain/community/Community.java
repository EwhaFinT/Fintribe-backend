package Fint.FinTribe.domain.community;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Builder
@Document(collection = "community")
public class Community {
    @Id
    private ObjectId communityId;
    private ObjectId artId;
    private Boolean isDeleted;
}

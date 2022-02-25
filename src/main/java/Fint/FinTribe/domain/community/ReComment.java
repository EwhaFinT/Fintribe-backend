package Fint.FinTribe.domain.community;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Document(collection = "reComment")
public class ReComment {
    private List<Integer> reCommentId;
    private String content;
    private ObjectId userId;
    private String identity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ObjectId tagUser;
    private String tagUserIdentity;
    private Boolean isDeleted;
}

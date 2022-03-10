package Fint.FinTribe.domain.community;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Document(collection = "reComment")
public class ReComment {
    @Id
    private Integer reCommentId;
    private Integer tagCommentId;
    private ObjectId articleId;
    private String content;
    private ObjectId userId;
    private String identity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ObjectId tagUser;
    private String tagUserIdentity;
    private Boolean isDeleted;
}

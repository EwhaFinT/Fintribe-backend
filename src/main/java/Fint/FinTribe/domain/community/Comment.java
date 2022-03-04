package Fint.FinTribe.domain.community;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Builder
@Document(collection = "comment")
public class Comment {
    private String content;
    private ObjectId userId;
    private String identity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ObjectId tagUser;
    private Boolean isDeleted;
}

package Fint.FinTribe.domain.community;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Document(collection = "comment")
public class Comment {
    @Id
    private Integer commentId;
    private ObjectId articleId;
    private String content;
    private ObjectId userId;
    private String identity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
}

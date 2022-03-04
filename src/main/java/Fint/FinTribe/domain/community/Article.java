package Fint.FinTribe.domain.community;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Document(collection = "article")
public class Article {
    private ObjectId userId;
    private String identity;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private List<List<Comment>> comments;
}
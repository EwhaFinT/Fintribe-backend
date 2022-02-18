package Fint.FinTribe.domain.community;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Document(collection = "article")
public class Article {
    private Long userId;        //User에서 Object 형식으로 바꿀지 생각
    private String identity;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private List<List<Comment>> comments;
}
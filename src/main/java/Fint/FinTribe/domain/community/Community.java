package Fint.FinTribe.domain.community;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Builder
@Document(collection = "community")
public class Community {
    private Long artId;
    private Boolean isDeleted;
    private List<Vote> voteList;
    private List<Article> articleList;
}

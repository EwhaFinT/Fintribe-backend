package Fint.FinTribe.payload.response;

import Fint.FinTribe.domain.community.Article;
import lombok.*;
import org.bson.types.ObjectId;
import java.util.List;

@Getter
@AllArgsConstructor
public class CommunityResponse {
    private String identity;
    private String communityId;
    private Boolean isDeleted;
    private List<Article> article;
}

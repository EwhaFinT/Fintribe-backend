package Fint.FinTribe.payload.response;

import Fint.FinTribe.domain.community.Article;
import Fint.FinTribe.domain.community.Comment;
import Fint.FinTribe.domain.community.ReComment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ArticleAndCommentResponse {
    Article article;
    String articleId;
    List<Comment> comments;
    List<ReComment> reComments;
}

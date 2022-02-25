package Fint.FinTribe.service.community;

import Fint.FinTribe.domain.community.Article;
import Fint.FinTribe.domain.community.Comment;
import Fint.FinTribe.domain.community.ReComment;
import Fint.FinTribe.domain.community.Community;
import Fint.FinTribe.domain.user.User;
import Fint.FinTribe.payload.response.CommunityResponse;
import Fint.FinTribe.repository.community.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final CommunityRepository communityRepository;
    private final MongoTemplate mongoTemplate;

    public Article textToArticle(Long articleId, ObjectId userId, String identity, String title, String content){
        LocalDateTime now = LocalDateTime.now();
        List<List<ReComment>> comments = new ArrayList<>();
        return new Article(articleId, userId, identity, title, content, now, now, false, comments);
    }

    public ReComment textToReComment(List<Integer> commentId, ObjectId userId, ObjectId tagUser, String content){
        User user = new user();     //user 찾는 함수 호출
        User tUser = new user();        //tagUser로 user 찾는 함수 호출
        LocalDateTime now = LocalDateTime.now();
        return new ReComment(commentId, content, userId, user.getIdentity(), now, now, tagUser, tUser.getIdentity(), false);
    }

    public Comment textToComment(List<Integer> commentId, ObjectId userId, String content){
        User user = new user();     //user 찾는 함수 호출
        LocalDateTime now = LocalDateTime.now();
        return new Comment(commentId, content, userId, user.getIdentity(), now, now, false);
    }

    //artId로 community 정보 불러오기
    public Community getCommunityByArtID(ObjectId artId){
        return communityRepository.findByArtIdAndIsDeleted(artId, false).get();
    }

    public Community getCommunityById(ObjectId communityId){
        return communityRepository.findById(communityId).get();
    }

    public CommunityResponse getCommunityInformation(ObjectId artId, ObjectId userId){
        Community community = getCommunityByArtID(artId);
        User user = new User();//userId로 identity 찾는 함수 호출
        return new CommunityResponse(user.getIdentity(), community.getCommunityId(), community.getIsDeleted(), community.getArticleList());
    }

    //articleList에 새로운 article 추가
    public void createArticle(ObjectId userId, String title, String content, ObjectId communityId){
        Community community = getCommunityById(communityId);
        User user = new User();     //userId로 identity 찾는 함수 호출

        Query query = new Query().addCriteria((Criteria.where("_id").is(communityId)));
        Update update = new Update();
        Long index;

        List<Article> articleList = community.getArticleList();

        if(articleList == null || articleList.isEmpty()){
            index = 0L;
        }
        else{
            index = Long.valueOf(articleList.size());
        }

        List<Article> updatedArticles = new ArrayList<>();

        Article article = textToArticle(index, userId, user.getIdentity(), title, content);
        updatedArticles.add(article);

        update.push("articleList").each(updatedArticles);
        mongoTemplate.updateFirst(query, update, "community");
    }

    //article 수정
    public void patchArticle(Long articleId, String title, String content, ObjectId communityId){
        LocalDateTime now = LocalDateTime.now();

        Query query = new Query().addCriteria(
                Criteria.where("_id").is(communityId)
        );
        query.addCriteria(
                Criteria.where("articleList.articleId").is(articleId)
        );

        Update update = new Update().set("articleList.$.title", title);
        update.set("articleList.$.content", content);
        update.set("articleList.$.updatedAt", now);
        mongoTemplate.updateFirst(query, update, "community");
    }

    //article 삭제
    public void deleteArticle(Long articleId, ObjectId communityId){
        Query query = new Query().addCriteria(
                Criteria.where("_id").is(communityId)
        );
        query.addCriteria(
                Criteria.where("articleList.articleId").is(articleId)
        );
        Update update = new Update().set("articleList.$.isDeleted", true);
        mongoTemplate.updateFirst(query, update, "community");
    }

    //comments 혹은 reComments에 새로운 comment 추가
    public void createComment(ObjectId userId, Long articleId, ObjectId tagUser, String content, ObjectId communityId, List<Integer> tagCommentId){
        Query query = new Query().addCriteria((Criteria.where("_id").is(communityId)));
        query.addCriteria(
                Criteria.where("articleList.articleId").is(articleId)
        );

        Update update = new Update();
        List<Integer> commentId = new ArrayList<>();

        if(tagCommentId.get(0) == -1){
            List<Comment> commentList = getCommunityById(communityId).getArticleList().get(articleId.intValue()).getComments();

            if(commentList == null || commentList.isEmpty()){
                commentId.add(0);
                commentId.add(0);
            }
            else{
                commentId.add(commentList.size());
                commentId.add(0);
            }

            List<Comment> updatedComments = new ArrayList<>();

            Comment comment = textToComment(commentId, userId, content);
            updatedComments.add(comment);

            update.push("articleList.$.comments").each(updatedComments);
            mongoTemplate.updateFirst(query, update, "community");
        }
        else{
            List<ReComment> commentList = getCommunityById(communityId).getArticleList().get(articleId.intValue()).getReComments();

            if(commentList == null || commentList.isEmpty()){
                commentId.add(tagCommentId.get(0));
                commentId.add(0);
            }
            else{
                commentId.add(tagCommentId.get(0));
                commentId.add(commentList.size());
            }

            List<ReComment> updatedComments = new ArrayList<>();

            ReComment comment = textToReComment(commentId, userId, tagUser, content);
            updatedComments.add(comment);

            update.push("articleList.$.reComments").each(updatedComments);
            mongoTemplate.updateFirst(query, update, "community");
        }
    }

    //comment 수정
    public void patchComment(Long articleId, List<Integer> commentId, ObjectId tagUser, String content, ObjectId communityId, List<Integer> tagCommentId){
        Query query = new Query().addCriteria((Criteria.where("_id").is(communityId)));
        query.addCriteria(
                Criteria.where("articleList.articleId").is(articleId)
        );
        Update update = new Update();

        LocalDateTime now = LocalDateTime.now();

        if (tagCommentId.get(0) == -1){
            query.addCriteria(Criteria.where("comments.commentId").is(commentId));
            update.set("articleList.$.comments.$.content", content);
            update.set("articleList.$.comments.$.updatedAt", now);
            mongoTemplate.updateFirst(query, update, "community");
        }
        else{
            query.addCriteria(Criteria.where("reComments.reCommentId").is(commentId));
            update.set("articleList.$.reComments.$.content", content);
            update.set("articleList.$.reComments.$.updatedAt", now);
            mongoTemplate.updateFirst(query, update, "community");
        }
    }

    //comment 삭제
    public void deleteComment(Long articleId, List<Integer> commentId, ObjectId communityId, List<Integer> tagCommentId){
        Query query = new Query().addCriteria((Criteria.where("_id").is(communityId)));
        query.addCriteria(
                Criteria.where("articleList.articleId").is(articleId)
        );
        Update update = new Update();

        if (tagCommentId.get(0) == -1){
            query.addCriteria(Criteria.where("comments.commentId").is(commentId));
            update.set("articleList.$.comments.$.isDeleted", true);
            mongoTemplate.updateFirst(query, update, "community");
        }
        else{
            query.addCriteria(Criteria.where("reComments.reCommentId").is(commentId));
            update.set("articleList.$.reComments.$.isDeleted", true);
            mongoTemplate.updateFirst(query, update, "community");
        }
    }
}

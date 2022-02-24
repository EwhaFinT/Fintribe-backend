package Fint.FinTribe.service.community;

import Fint.FinTribe.domain.community.Article;
import Fint.FinTribe.domain.community.Comment;
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
        List<List<Comment>> comments = new ArrayList<>();
        return new Article(articleId, userId, identity, title, content, now, now, false, comments);
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

        Query query = new Query().addCriteria((Criteria.where("_id").is(community.getCommunityId())));
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
        Query query = new Query().addCriteria(
                Criteria.where("_id").is(communityId)
        );
        query.addCriteria(
                Criteria.where("articleList.articleId").is(articleId)
        );
        Update update = new Update().set("articleList.$.title", title);
        update.addToSet("articleList.$.content", content);
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
}

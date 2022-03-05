package Fint.FinTribe.service.community;

import Fint.FinTribe.domain.community.*;
import Fint.FinTribe.domain.user.User;
import Fint.FinTribe.payload.request.*;
import Fint.FinTribe.payload.response.CommunityResponse;
import Fint.FinTribe.payload.response.VoteCheckResponse;
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
        List<Comment> comments = new ArrayList<>();
        List<ReComment> reComments = new ArrayList<>();
        return new Article(articleId, userId, identity, title, content, now, now, false, comments, reComments);
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

    public Vote textToVote(Integer voteId, ObjectId userId, String title, Double price, LocalDateTime endTime){
        User user = new User();     //user 찾는 함수
        LocalDateTime now = LocalDateTime.now();
        List<ParticipantVote> participantVoteList = new ArrayList<>();
        return new Vote(voteId, userId, user.getIdentity(), title, price, now, endTime, false, 0.0, 0.0, participantVoteList);
    }

    public ParticipantVote textToParticipant(ObjectId userId, Boolean choice, Double ratio){
        LocalDateTime now = LocalDateTime.now();
        return new ParticipantVote(userId, choice, now, ratio);
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
    public void createArticle(ArticleRequest articleRequest){
        Community community = getCommunityById(articleRequest.getCommunityId());
        User user = new User();     //userId로 identity 찾는 함수 호출

        Query query = new Query().addCriteria((Criteria.where("_id").is(articleRequest.getCommunityId())));
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

        Article article = textToArticle(index, articleRequest.getUserId(), user.getIdentity(), articleRequest.getTitle(), articleRequest.getContent());
        updatedArticles.add(article);

        update.push("articleList").each(updatedArticles);
        mongoTemplate.updateFirst(query, update, "community");
    }

    //article 수정
    public void patchArticle(ReviseArticleRequest articleRequest){
        LocalDateTime now = LocalDateTime.now();

        Query query = new Query().addCriteria(
                Criteria.where("_id").is(articleRequest.getCommunityId())
        );
        query.addCriteria(
                Criteria.where("articleList.articleId").is(articleRequest.getArticleId())
        );

        Update update = new Update().set("articleList.$.title", articleRequest.getTitle());
        update.set("articleList.$.content", articleRequest.getContent());
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
    public void createComment(CommentRequest commentRequest){
        Query query = new Query().addCriteria((Criteria.where("_id").is(commentRequest.getCommunityId())));
        query.addCriteria(
                Criteria.where("articleList.articleId").is(commentRequest.getArticleId())
        );

        Update update = new Update();
        List<Integer> commentId = new ArrayList<>();

        if(commentRequest.getTagCommentId().get(0) == -1){
            List<Comment> commentList = getCommunityById(commentRequest.getCommunityId()).getArticleList().get(commentRequest.getArticleId().intValue()).getComments();

            if(commentList == null || commentList.isEmpty()){
                commentId.add(0);
                commentId.add(0);
            }
            else{
                commentId.add(commentList.size());
                commentId.add(0);
            }

            List<Comment> updatedComments = new ArrayList<>();

            Comment comment = textToComment(commentId, commentRequest.getUserId(), commentRequest.getContent());
            updatedComments.add(comment);

            update.push("articleList.$.comments").each(updatedComments);
            mongoTemplate.updateFirst(query, update, "community");
        }
        else{
            List<ReComment> commentList = getCommunityById(commentRequest.getCommunityId()).getArticleList().get(commentRequest.getArticleId().intValue()).getReComments();

            if(commentList == null || commentList.isEmpty()){
                commentId.add(commentRequest.getTagCommentId().get(0));
                commentId.add(0);
            }
            else{
                commentId.add(commentRequest.getTagCommentId().get(0));
                commentId.add(commentList.size());
            }

            List<ReComment> updatedComments = new ArrayList<>();

            ReComment comment = textToReComment(commentId, commentRequest.getUserId(), commentRequest.getTagUser(), commentRequest.getContent());
            updatedComments.add(comment);

            update.push("articleList.$.reComments").each(updatedComments);
            mongoTemplate.updateFirst(query, update, "community");
        }
    }

    //comment 수정
    public void patchComment(ReviseCommentRequest commentRequest){
        Query query = new Query().addCriteria((Criteria.where("_id").is(commentRequest.getCommunityId())));
        query.addCriteria(
                Criteria.where("articleList.articleId").is(commentRequest.getArticleId())
        );
        Update update = new Update();

        LocalDateTime now = LocalDateTime.now();

        if (commentRequest.getTagCommetId().get(0) == -1){
            query.addCriteria(Criteria.where("comments.commentId").is(commentRequest.getCommentId()));
            update.set("articleList.$.comments.$.content", commentRequest.getContent());
            update.set("articleList.$.comments.$.updatedAt", now);
            mongoTemplate.updateFirst(query, update, "community");
        }
        else{
            query.addCriteria(Criteria.where("reComments.reCommentId").is(commentRequest.getCommentId()));
            update.set("articleList.$.reComments.$.content", commentRequest.getContent());
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

    //vote 추가
    public void createVote(VoteProposalRequest voteRequest){
        Community community = getCommunityById(voteRequest.getCommunityId());

        Query query = new Query().addCriteria((Criteria.where("_id").is(voteRequest.getCommunityId())));
        Update update = new Update();
        Integer index;

        List<Vote> voteList = community.getVoteList();

        if(voteList == null || voteList.isEmpty()){
            index = 0;
        }
        else{
            index = voteList.size();
        }

        List<Vote> updatedVotes = new ArrayList<>();

        Vote vote = textToVote(index, voteRequest.getUserId(), voteRequest.getTitle(), voteRequest.getResalePrice(), voteRequest.getEndTime());
        updatedVotes.add(vote);

        update.push("voteList").each(updatedVotes);
        mongoTemplate.updateFirst(query, update, "community");
    }

    //vote 참여
    public String participateVote(VoteRequest voteRequest){
        Query query = new Query().addCriteria((Criteria.where("_id").is(voteRequest.getCommunityId())));
        query.addCriteria(Criteria.where("voteList.voteId").is(voteRequest.getVoteId()));

        Update update = new Update();

        ObjectId artId = getCommunityById(voteRequest.getCommunityId()).getArtId();
        Double ratio = 0.0;// artId와 userId로 지분 조회하는 함수

        ParticipantVote participantVote = textToParticipant(voteRequest.getUserId(), voteRequest.getChoice(), ratio);
        List<ParticipantVote> participantList = getCommunityById(voteRequest.getCommunityId()).getVoteList().get(voteRequest.getVoteId()).getParticipants();
        if(!participantList.contains(participantVote)){
            Vote vote = getCommunityById(voteRequest.getCommunityId()).getVoteList().get(voteRequest.getVoteId());

            if(voteRequest.getChoice()){
                Update voteUpdate = new Update().set("voteList.$.agreement", vote.getAgreement() + ratio);
                mongoTemplate.updateFirst(query, voteUpdate, "community");
            }
            else {
                Update voteUpdate = new Update().set("voteList.$.disagreement", vote.getDisagreement() + ratio);
                mongoTemplate.updateFirst(query, voteUpdate, "community");
            }

            List<ParticipantVote> updatedParticipant = new ArrayList<>();
            updatedParticipant.add(participantVote);

            update.push("articleList.$.comments").each(updatedParticipant);
            mongoTemplate.updateFirst(query, update, "community");
            return "Success";
        }
        return "Duplicate Vote";
    }

    //투표창 조회
    public VoteCheckResponse getVoteInformation(ObjectId communityId, Integer voteId){
        Vote vote = getCommunityById(communityId).getVoteList().get(voteId);
        return new VoteCheckResponse(vote.getVoteId(), vote.getUserId(), vote.getIdentity(),
                vote.getTitle(), vote.getResalePrice(), vote.getStartTime(), vote.getEndTime(),
                vote.getIsDeleted(), vote.getAgreement(), vote.getDisagreement());
    }
}

package Fint.FinTribe.service;

import Fint.FinTribe.domain.art.Art;
import Fint.FinTribe.domain.community.*;
import Fint.FinTribe.domain.user.User;
import Fint.FinTribe.payload.request.*;
import Fint.FinTribe.payload.response.*;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final CommunityRepository communityRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final ReCommentRepository reCommentRepository;
    private final VoteRepository voteRepository;
    private final ParticipantVoteRepository participantVoteRepository;
    private final UserService userService;
    private final ArtService artService;

    //DB Article 조회
    private List<Article> findArticlesByCommunityId(ObjectId communityId){
        return articleRepository.findByCommunityIdAndIsDeleted(communityId, false);
    }

    //DB Article 개별 조회
    //TODO; error ctrl
    private Article findArticleById(ObjectId articleId){
        return articleRepository.findByArticleIdAndIsDeleted(articleId, false).orElseThrow();
    }

    //Community entity 생성
    private Community communityToEntity(ObjectId artId){
        return Community.builder()
                .artId(artId)
                .isDeleted(false)
                .build();
    }

    //Article entity 생성
    private Article articleRequestToEntity(ArticleRequest articleRequest){
        LocalDateTime now = LocalDateTime.now();
        return Article.builder()
                .communityId(articleRequest.getCommunityId())
                .userId(new ObjectId(articleRequest.getUserId()))
                .identity(articleRequest.getIdentity())
                .title(articleRequest.getTitle())
                .content(articleRequest.getContent())
                .createdAt(now)
                .updatedAt(now)
                .isDeleted(false)
                .build();
    }

    //Comment entity 생성
    private Comment commentRequestToEntity(CommentRequest commentRequest, Integer commentId){
        LocalDateTime now = LocalDateTime.now();
        ObjectId userId = new ObjectId(commentRequest.getUserId());
        ObjectId articleId = new ObjectId(commentRequest.getArticleId());
        User user = userService.findByUserId(userId).get();
        return Comment.builder()
                .commentId(commentId)
                .articleId(articleId)
                .content(commentRequest.getContent())
                .userId(userId)
                .identity(user.getIdentity())
                .createdAt(now)
                .updatedAt(now)
                .isDeleted(false)
                .build();
    }

    //ReComment entity 생성
    private ReComment reCommentRequestToEntity(CommentRequest commentRequest, Integer commentId){
        LocalDateTime now = LocalDateTime.now();
        User user = userService.findByUserId(new ObjectId(commentRequest.getUserId())).get();
        User tagUser = userService.findByUserId(new ObjectId(commentRequest.getUserId())).get();
        return ReComment.builder()
                .reCommentId(commentId)
                .tagCommentId(commentRequest.getTagCommentId())
                .articleId(new ObjectId(commentRequest.getArticleId()))
                .content(commentRequest.getContent())
                .userId(new ObjectId(commentRequest.getUserId()))
                .identity(user.getIdentity())
                .createdAt(now)
                .updatedAt(now)
                .tagUser(new ObjectId(commentRequest.getTagUser()))
                .tagUserIdentity(tagUser.getIdentity())
                .isDeleted(false)
                .build();
    }

    //Vote entity 생성
    private Vote voteProposalRequestToEntity(VoteProposalRequest voteRequest){
        LocalDateTime now = LocalDateTime.now();
        User user = userService.findByUserId(new ObjectId(voteRequest.getUserId())).get();
        return Vote.builder()
                .communityId(new ObjectId(voteRequest.getCommunityId()))
                .userId(new ObjectId(voteRequest.getUserId()))
                .identity(user.getIdentity())
                .title(voteRequest.getTitle())
                .resalePrice(voteRequest.getResalePrice())
                .startTime(now)
                .endTime(voteRequest.getEndTime())
                .isDeleted(false)
                .agreement(0.0)
                .disagreement(0.0)
                .build();
    }

    //ParticipantVote 객체 생성
    private ParticipantVote voteRequestToEntity(VoteRequest voteRequest, Double ratio){
        LocalDateTime now = LocalDateTime.now();
        return new ParticipantVote(new ObjectId(voteRequest.getVoteId()), new ObjectId(voteRequest.getUserId()), voteRequest.getChoice(), now, ratio);
    }

    //Community 조회
//    public CommunityResponse getCommunity(ObjectId artId, ObjectId userId){
//        Community community = findCommunityByArtId(artId);
//        User user = userService.findByUserId(userId).get(); // == User find 함수 호출
//        return new CommunityResponse(user.getIdentity(), community.getCommunityId().toString(), community.getIsDeleted(), findArticlesByCommunityId(community.getCommunityId()));
//    }

    //유저가 방문할 수 있는 커뮤니티 조회
    public CommunitiesResponse getCommunities(String userId){
        List<ObjectId> artIdList = userService.getArtId(new ObjectId(userId));
        List<String> responseList = new ArrayList<>();
        for(ObjectId artId : artIdList){
            responseList.add(artId.toString());
        }
        return new CommunitiesResponse(responseList);
    }

    //커뮤니티 내 미술품 정보 받기
    public CommunityResponse getCommunity(String communityId){
        Community community = communityRepository.findById(new ObjectId(communityId)).orElseThrow();
        Art art = artService.findArtById(community.getArtId());
        return new CommunityResponse(art.getPainter(), art.getArtName(), art.getDetail(), art.getPrice(), art.getNftAdd(), art.getPaint());
    }

    //게시글 목록 받기
    public ArticlesResponse getArticleList(String communityId){
        List<Article> articleList = findArticlesByCommunityId(new ObjectId(communityId));
        Map<String, String> articles = new HashMap<>();
        for(Article article : articleList){
            articles.put(article.getArticleId().toString(), article.getTitle());
        }
        return new ArticlesResponse(articles);
    }

    //댓글, 대댓글도 같이 들고 오기
    public ArticleAndCommentResponse getArticle(String articleId){
        ObjectId id = new ObjectId(articleId);
        Article article = findArticleById(id);
        return new ArticleAndCommentResponse(article, article.getArticleId().toString(), commentRepository.findByArticleId(id), reCommentRepository.findByArticleId(id));
    }

    //Article 생성
    public ArticleResponse createArticle(ArticleRequest articleRequest){
        articleRepository.save(articleRequestToEntity(articleRequest));
        return new ArticleResponse("success");
    }

    //Article 수정
    public ArticleResponse updateArticle(ReviseArticleRequest articleRequest){
        LocalDateTime now = LocalDateTime.now();

        Optional<Article> articleOp = articleRepository.findById(new ObjectId(articleRequest.getArticleId()));
        if(!articleOp.isPresent()){
            return new ArticleResponse("No such article");
        }
        Article article = articleOp.get();
        article.setTitle(articleRequest.getTitle());
        article.setContent(articleRequest.getContent());
        article.setUpdatedAt(now);
        articleRepository.save(article);
        return new ArticleResponse("success");
    }

    //Article 삭제
    public ArticleResponse deleteArticle(String articleId){
        Optional<Article> articleOp = articleRepository.findById(new ObjectId(articleId));
        if(!articleOp.isPresent()){
            return new ArticleResponse("No such article");
        }
        Article article = articleOp.get();
        article.setIsDeleted(true);
        articleRepository.save(article);
        return new ArticleResponse("success");
    }

    //Comment 생성
    public CommentResponse createComment(CommentRequest commentRequest){
        Integer lastComment;
        if(commentRequest.getTagCommentId() == -1){
            List<Comment> commentList = commentRepository.findByArticleId(new ObjectId(commentRequest.getArticleId()));
            if(commentList.isEmpty())
                lastComment = -1;
            else
                lastComment = commentList.get(commentList.size() - 1).getCommentId();
            commentRepository.save(commentRequestToEntity(commentRequest, lastComment + 1));
        }
        else{
            if(commentRepository.findById(commentRequest.getTagCommentId()).get().getIsDeleted()){
                return new CommentResponse("deleted comment");
            }
            List<ReComment> commentList = reCommentRepository.findByArticleIdAndTagCommentId(new ObjectId(commentRequest.getArticleId()), commentRequest.getTagCommentId());
            if(commentList.isEmpty())
                lastComment = -1;
            else
                lastComment = commentList.get(commentList.size() - 1).getReCommentId();
            reCommentRepository.save(reCommentRequestToEntity(commentRequest, lastComment + 1));
        }
        return new CommentResponse("success");
    }

    //Comment 수정
    public CommentResponse updateComment(ReviseCommentRequest commentRequest){
        LocalDateTime now = LocalDateTime.now();
        if(commentRequest.getTagCommentId() == -1){
            Optional<Comment> commentOp = commentRepository.findByIdAndArticleId(commentRequest.getCommentId(), new ObjectId(commentRequest.getArticleId()));
            if(!commentOp.isPresent()){
                return new CommentResponse("No Such Comment");
            }
            Comment comment = commentOp.get();
            comment.setContent(commentRequest.getContent());
            comment.setUpdatedAt(now);
            commentRepository.save(comment);
        }
        else{
            Optional<ReComment> commentOp = reCommentRepository.findByIdAndTagCommentIdAndArticleId(commentRequest.getCommentId(), commentRequest.getTagCommentId(), new ObjectId(commentRequest.getArticleId()));
            if(!commentOp.isPresent()){
                return new CommentResponse("No Such Comment");
            }
            ReComment comment = commentOp.get();
            comment.setContent(commentRequest.getContent());
            comment.setUpdatedAt(now);
            reCommentRepository.save(comment);
        }
        return new CommentResponse("success");
    }

    //Comment 삭제
    public CommentResponse deleteComment(String articleId, Integer commentId, Integer tagCommentId){
        ObjectId id = new ObjectId(articleId);
        if(tagCommentId == -1){
            Optional<Comment> commentOp = commentRepository.findByIdAndArticleId(commentId, id);
            if(!commentOp.isPresent()){
                return new CommentResponse("No Such Comment");
            }
            Comment comment = commentOp.get();
            comment.setIsDeleted(true);
            commentRepository.save(comment);
        }
        else{
            Optional<ReComment> commentOp = reCommentRepository.findByIdAndTagCommentIdAndArticleId(commentId, tagCommentId, id);
            if(!commentOp.isPresent()){
                return new CommentResponse("No Such Comment");
            }
            ReComment comment = commentOp.get();
            comment.setIsDeleted(true);
            reCommentRepository.save(comment);
        }
        return new CommentResponse("success");
    }

    //Vote 생성
    public VoteResponse createVote(VoteProposalRequest voteRequest){
        voteRepository.save(voteProposalRequestToEntity(voteRequest));
        return new VoteResponse("success");
    }

    //Vote 참여
    public VoteResponse participateVote(VoteRequest voteRequest){
        Double ratio = 10.0; //-- artId와 userId로 지분 조회하는 함수 호출

        Optional<ParticipantVote> participant = participantVoteRepository.findByVoteIdAndUserId(new ObjectId(voteRequest.getVoteId()), new ObjectId(voteRequest.getUserId()));
        if(!participant.isPresent()){
            ParticipantVote newParticipant = voteRequestToEntity(voteRequest, ratio);
            Vote vote = voteRepository.findById(new ObjectId(voteRequest.getVoteId())).get();
            if(voteRequest.getChoice()){
                vote.setAgreement(vote.getAgreement() + ratio);
                if(vote.getAgreement() > 0.5){
                    vote.setEndTime(LocalDateTime.now());
                    //auction에 올리는 함수 추가
                }
            }
            else{
                vote.setDisagreement(vote.getDisagreement() + ratio);
                if(vote.getDisagreement() > 0.5){
                    vote.setEndTime(LocalDateTime.now());
                }
            }
            voteRepository.save(vote);
            participantVoteRepository.save(newParticipant);
            return new VoteResponse("success");
        }
        return new VoteResponse("duplicate vote");
    }

    //Vote 조회
    public VoteCheckResponse getVoteInformation(String voteId){
        Optional<Vote> voteOp = voteRepository.findById(new ObjectId(voteId));
        if(!voteOp.isPresent()){
            return new VoteCheckResponse("No such vote");
        }
        Vote vote = voteOp.get();
        return new VoteCheckResponse(voteId, vote.getUserId().toString(), vote.getIdentity(),
                vote.getTitle(), vote.getResalePrice(), vote.getStartTime(), vote.getEndTime(),
                vote.getIsDeleted(), vote.getAgreement(), vote.getDisagreement());
    }

    //Community 생성
    public void createCommunity(ObjectId artId){
        communityRepository.save(communityToEntity(artId));
    }
}
package Fint.FinTribe.service.community;

import Fint.FinTribe.domain.community.*;
import Fint.FinTribe.domain.user.User;
import Fint.FinTribe.payload.request.*;
import Fint.FinTribe.payload.response.*;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    //DB community 조회
    private Community findCommunityByArtId(ObjectId artId){
        return communityRepository.findByArtIdAndIsDeleted(artId, false).get();
    }

    //DB Article 조회
    private List<Article> findArticlesByCommunityId(ObjectId communityId){
        return articleRepository.findByCommunityIdAndIsDeleted(communityId, false);
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
                .userId(articleRequest.getUserId())
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
        User user = userService.findByUserId(commentRequest.getUserId()).get();
        return Comment.builder()
                .commentId(commentId)
                .articleId(commentRequest.getArticleId())
                .content(commentRequest.getContent())
                .userId(commentRequest.getUserId())
                .identity(user.getIdentity())
                .createdAt(now)
                .updatedAt(now)
                .isDeleted(false)
                .build();
    }

    //ReComment entity 생성
    private ReComment reCommentRequestToEntity(CommentRequest commentRequest, Integer commentId){
        LocalDateTime now = LocalDateTime.now();
        User user = userService.findByUserId(commentRequest.getUserId()).get();
        User tagUser = userService.findByUserId(commentRequest.getTagUser()).get();
        return ReComment.builder()
                .reCommentId(commentId)
                .tagCommentId(commentRequest.getTagCommentId())
                .articleId(commentRequest.getArticleId())
                .content(commentRequest.getContent())
                .userId(commentRequest.getUserId())
                .identity(user.getIdentity())
                .createdAt(now)
                .updatedAt(now)
                .tagUser(commentRequest.getTagUser())
                .tagUserIdentity(tagUser.getIdentity())
                .isDeleted(false)
                .build();
    }

    //Vote entity 생성
    private Vote voteProposalRequestToEntity(VoteProposalRequest voteRequest){
        LocalDateTime now = LocalDateTime.now();
        User user = userService.findByUserId(voteRequest.getUserId()).get();
        return Vote.builder()
                .communityId(voteRequest.getCommunityId())
                .userId(voteRequest.getUserId())
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
        return new ParticipantVote(voteRequest.getVoteId(), voteRequest.getUserId(), voteRequest.getChoice(), now, ratio);
    }

    //Community 조회
    public CommunityResponse getCommunity(ObjectId artId, ObjectId userId){
        Community community = findCommunityByArtId(artId);
        User user = userService.findByUserId(userId).get(); // == User find 함수 호출
        return new CommunityResponse(user.getIdentity(), community.getCommunityId().toString(), community.getIsDeleted(), findArticlesByCommunityId(community.getCommunityId()));
    }

    //Article 생성
    public ArticleResponse createArticle(ArticleRequest articleRequest){
        articleRepository.save(articleRequestToEntity(articleRequest));
        return new ArticleResponse("success");
    }

    //Article 수정
    public ArticleResponse updateArticle(ReviseArticleRequest articleRequest){
        LocalDateTime now = LocalDateTime.now();

        Article article = articleRepository.findById(articleRequest.getArticleId()).get();
        article.setTitle(articleRequest.getTitle());
        article.setContent(articleRequest.getContent());
        article.setUpdatedAt(now);
        articleRepository.save(article);
        return new ArticleResponse("success");
    }

    //Article 삭제
    public ArticleResponse deleteArticle(ObjectId articleId){
        Article article = articleRepository.findById(articleId).get();
        article.setIsDeleted(true);
        articleRepository.save(article);
        return new ArticleResponse("success");
    }

    //Comment 생성
    public CommentResponse createComment(CommentRequest commentRequest){
        Integer lastComment;
        if(commentRequest.getTagCommentId() == -1){
            List<Comment> commentList = commentRepository.findByArticleId(commentRequest.getArticleId());
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
            List<ReComment> commentList = reCommentRepository.findByTagCommentId(commentRequest.getTagCommentId());
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
            Comment comment = commentRepository.findByIdAndArticleId(commentRequest.getCommentId(), commentRequest.getArticleId()).get();
            comment.setContent(commentRequest.getContent());
            comment.setUpdatedAt(now);
            commentRepository.save(comment);
        }
        else{
            ReComment comment = reCommentRepository.findByIdAndTagCommentIdAndArticleId(commentRequest.getCommentId(), commentRequest.getTagCommentId(), commentRequest.getArticleId()).get();
            comment.setContent(commentRequest.getContent());
            comment.setUpdatedAt(now);
            reCommentRepository.save(comment);
        }
        return new CommentResponse("success");
    }

    //Comment 삭제
    public CommentResponse deleteComment(ObjectId articleId, Integer commentId, Integer tagCommentId){
        if(tagCommentId == -1){
            Comment comment = commentRepository.findByIdAndArticleId(commentId, articleId).get();
            comment.setIsDeleted(true);
            commentRepository.save(comment);
        }
        else{
            ReComment comment = reCommentRepository.findByIdAndTagCommentIdAndArticleId(commentId, tagCommentId, articleId).get();
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

        Optional<ParticipantVote> participant = participantVoteRepository.findByVoteIdAndUserId(voteRequest.getVoteId(), voteRequest.getUserId());
        if(!participant.isPresent()){
            ParticipantVote newParticipant = voteRequestToEntity(voteRequest, ratio);
            Vote vote = voteRepository.findById(voteRequest.getVoteId()).get();
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
    public VoteCheckResponse getVoteInformation(ObjectId voteId){
        Vote vote = voteRepository.findById(voteId).get();
        return new VoteCheckResponse(voteId.toString(), vote.getUserId().toString(), vote.getIdentity(),
                vote.getTitle(), vote.getResalePrice(), vote.getStartTime(), vote.getEndTime(),
                vote.getIsDeleted(), vote.getAgreement(), vote.getDisagreement());
    }

    //Community 생성
    public void createCommunity(ObjectId artId){
        communityRepository.save(communityToEntity(artId));
    }
}

package Fint.FinTribe.service;

import Fint.FinTribe.domain.art.Art;
import Fint.FinTribe.domain.art.ArtRepository;
import Fint.FinTribe.domain.auctionDate.AuctionDate;
import Fint.FinTribe.domain.auctionDate.AuctionDateRepository;
import Fint.FinTribe.domain.community.*;
import Fint.FinTribe.domain.resaleDate.ResaleDate;
import Fint.FinTribe.domain.resaleDate.ResaleDateRepository;
import Fint.FinTribe.domain.user.User;
import Fint.FinTribe.payload.request.*;
import Fint.FinTribe.payload.response.*;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final CommunityRepository communityRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final ReCommentRepository reCommentRepository;
    private final VoteRepository voteRepository;
    private final ParticipantVoteRepository participantVoteRepository;
    private final ArtRepository artRepository;
    private final ResaleDateRepository resaleDateRepository;
    private final UserService userService;

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
    private Community communityToEntity(ObjectId artId, List<ObjectId> userIdList, List<Double> ratioList){
        return Community.builder()
                .artId(artId)
                .isDeleted(false)
                .userIdList(userIdList)
                .ratioList(ratioList)
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
    public List<CommunitiesResponse> getCommunities(String userId){
        List<ObjectId> artIdList = userService.getArtId(new ObjectId(userId));
        List<CommunitiesResponse> responseList = new ArrayList<>();
        if(artIdList.isEmpty()){
            return new ArrayList<>();
        }
        for(ObjectId artId : artIdList){
            Art art = artRepository.findById(artId).get();
            communityRepository.findByArtIdAndIsDeleted(artId, false).ifPresent(community -> {
                CommunitiesResponse response = new CommunitiesResponse(community.getCommunityId().toString(), art.getArtName());
                responseList.add(response);
            });
        }
        return responseList;
    }

    //커뮤니티 내 미술품 정보 받기
    public CommunityResponse getCommunity(String communityId){
        Community community = communityRepository.findById(new ObjectId(communityId)).orElseThrow();
        Art art = artRepository.findById(community.getArtId()).get();
        return new CommunityResponse(art.getPainter(), art.getArtName(), art.getDetail(), art.getPrice(), art.getNftAdd(), art.getPaint());
    }

    //게시글 목록 받기
    public ArticlesResponse getArticleList(String communityId){
        List<Article> articleList = findArticlesByCommunityId(new ObjectId(communityId));
        List<ArticlesResTmp> articles = new ArrayList<>();
        for(Article article : articleList){
            articles.add(new ArticlesResTmp(article.getArticleId().toString(), article.getTitle(), article.getCreatedAt()));
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
        int flag = countArtwork(voteRequest.getEndTime());
        if(flag < 3){
            voteRepository.save(voteProposalRequestToEntity(voteRequest));
            return new VoteResponse("success");
        }
        else
            return new VoteResponse("경매 가능한 일자가 아닙니다.");
    }

    //Vote 참여
    public VoteResponse participateVote(VoteRequest voteRequest){
        Double ratio = getRatio(new ObjectId(voteRequest.getVoteId()), new ObjectId(voteRequest.getUserId()));

        Optional<ParticipantVote> participant = participantVoteRepository.findByVoteIdAndUserId(new ObjectId(voteRequest.getVoteId()), new ObjectId(voteRequest.getUserId()));
        if(!participant.isPresent()){
            ParticipantVote newParticipant = voteRequestToEntity(voteRequest, ratio);
            Vote vote = voteRepository.findById(new ObjectId(voteRequest.getVoteId())).get();
            if(voteRequest.getChoice()){
                vote.setAgreement(vote.getAgreement() + ratio);
                if(vote.getAgreement() > 0.5){
                    vote.setEndTime(LocalDateTime.now());
                    Optional<ResaleDate> resale = resaleDateRepository.findByResaleDate(vote.getEndTime().toLocalDate().plusDays(1L));
                    if(resale.isPresent()){
                        ResaleDate resaleDate = resale.get();
                        List<ObjectId> artIdList = resaleDate.getArtId();
                        artIdList.add(communityRepository.findById(vote.getCommunityId()).get().getArtId());
                        resaleDate.setArtId(artIdList);
                        resaleDateRepository.save(resaleDate);
                    }
                    else{
                        saveResaleDate(vote.getEndTime().toLocalDate().plusDays(1L), communityRepository.findById(vote.getCommunityId()).get().getArtId());
                    }
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
    public VoteCheckResponse getVoteInformation(String voteId, String userId){
        Optional<Vote> voteOp = voteRepository.findById(new ObjectId(voteId));
        Double ratio = getRatio(new ObjectId(voteId), new ObjectId(userId));
        if(!voteOp.isPresent()){
            return new VoteCheckResponse("No such vote");
        }
        Vote vote = voteOp.get();
        return new VoteCheckResponse(voteId, vote.getUserId().toString(), vote.getIdentity(),
                vote.getTitle(), vote.getResalePrice(), vote.getStartTime(), vote.getEndTime(),
                vote.getIsDeleted(), vote.getAgreement(), vote.getDisagreement(), ratio);
    }

    //Community 생성
    public void createCommunity(ObjectId artId, List<ObjectId> userIdList, List<Double> ratioList){
        communityRepository.save(communityToEntity(artId, userIdList, ratioList));
    }

    //userId로 지분 조회
    private Double getRatio(ObjectId voteId, ObjectId userId){
        Optional<Vote> voteOptional = voteRepository.findById(voteId);
        AtomicReference<Double> ratio = new AtomicReference<>(0.0);
        if(voteOptional.isPresent()){
            ObjectId communityId = voteOptional.get().getCommunityId();
            communityRepository.findById(communityId).ifPresent(community -> {
                int userIndex = community.getUserIdList().indexOf(userId);
                ratio.set(community.getRatioList().get(userIndex));
            });
        }
        return ratio.get();
    }

    private int countArtwork(LocalDateTime date) {
        List<Vote> voteList = voteRepository.findVotesByEndTime(date);
        return voteList.size();
    }

    //새로운 resaledate 저장
    private ResaleDate saveResaleDate(LocalDate resaleDate, ObjectId artId){
        List<ObjectId> artIdList = new ArrayList<>();
        artIdList.add(artId);
        return ResaleDate.builder()
                .resaleDate(resaleDate)
                .artId(artIdList)
                .build();
    }
}

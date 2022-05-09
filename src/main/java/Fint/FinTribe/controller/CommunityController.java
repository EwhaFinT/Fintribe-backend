package Fint.FinTribe.controller;

import Fint.FinTribe.payload.request.*;
import Fint.FinTribe.payload.response.*;
import Fint.FinTribe.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    //TODO; communtity 관련 api 수정 및 추가 필요
    //커뮤니티
    @GetMapping("/communities")
    public ResponseEntity<?> getCommunities(@RequestParam @Valid String userId){
        CommunitiesResponse communitiesResponse = communityService.getCommunities(userId);
        return new ResponseEntity<>(communitiesResponse, HttpStatus.OK);
    }

    @GetMapping("/community")
    public ResponseEntity<?> getCommunityInformation(@RequestParam @Valid String communityId){
        CommunityResponse communityResponse = communityService.getCommunity(communityId);
        return new ResponseEntity<>(communityResponse, HttpStatus.OK);
    }

    //게시글
    @GetMapping("/articles")
    public ResponseEntity<?> getArticles(@RequestParam @Valid String communityId){
        ArticlesResponse articlesResponse = communityService.getArticleList(communityId);
        return new ResponseEntity<>(articlesResponse, HttpStatus.OK);
    }

    @GetMapping("/article")
    public ResponseEntity<?> getArticle(@RequestParam @Valid String articleId){
        ArticleAndCommentResponse articleResponse = communityService.getArticle(articleId);
        return new ResponseEntity<>(articleResponse, HttpStatus.OK);
    }

    @PostMapping("/article")
    public ResponseEntity<?> postArticle(@RequestBody @Valid ArticleRequest articleRequest){
        ArticleResponse articleResponse = communityService.createArticle(articleRequest);
        return new ResponseEntity<>(articleResponse, HttpStatus.OK);
    }

    @PostMapping("/revise-article")
    public ResponseEntity<?> patchArticle(@RequestBody @Valid ReviseArticleRequest articleRequest){
        ArticleResponse articleResponse =  communityService.updateArticle(articleRequest);
        return new ResponseEntity<>(articleResponse, HttpStatus.OK);
    }

    @DeleteMapping("/delete-article")
    public ResponseEntity<?> deleteArticle(@RequestParam @Valid String articleId){
        ArticleResponse articleResponse =  communityService.deleteArticle(articleId);
        return new ResponseEntity<>(articleResponse, HttpStatus.OK);
    }

    //댓글
    @PostMapping("/comment")
    public ResponseEntity<?> postComment(@RequestBody @Valid CommentRequest commentRequest){
        CommentResponse commentResponse = communityService.createComment(commentRequest);
        return new ResponseEntity<>(commentResponse, HttpStatus.OK);
    }

    @PostMapping("/revise-comment")
    public ResponseEntity<?> patchComment(@RequestBody @Valid ReviseCommentRequest commentRequest){
        CommentResponse commentResponse = communityService.updateComment(commentRequest);
        return new ResponseEntity<>(commentResponse, HttpStatus.OK);
    }

    @DeleteMapping("/delete-comment")
    public ResponseEntity<?> deleteComment(@RequestParam @Valid String articleId, @RequestParam @Valid Integer commentId, @RequestParam @Valid Integer tagCommentId){
        CommentResponse commentResponse = communityService.deleteComment(articleId, commentId, tagCommentId);
        return new ResponseEntity<>(commentResponse, HttpStatus.OK);
    }

    //투표
    @PostMapping("/vote-proposal")
    public ResponseEntity<?> postVote(@RequestBody @Valid VoteProposalRequest voteRequest){
        VoteResponse voteResponse = communityService.createVote(voteRequest);
        return new ResponseEntity<>(voteResponse, HttpStatus.OK);
    }

    @PostMapping("/vote")
    public ResponseEntity<?> participateVote(@RequestBody @Valid VoteRequest voteRequest){
        VoteResponse voteResponse = communityService.participateVote(voteRequest);
        return new ResponseEntity<>(voteResponse, HttpStatus.OK);
    }

    @GetMapping("/check")
    public ResponseEntity<?> getVote(@RequestParam @Valid String voteId, @RequestParam @Valid String userId){
        VoteCheckResponse voteResponse = communityService.getVoteInformation(voteId, userId);
        return new ResponseEntity<>(voteResponse, HttpStatus.OK);
    }
}

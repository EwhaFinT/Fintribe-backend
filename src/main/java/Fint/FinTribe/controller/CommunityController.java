package Fint.FinTribe.controller;

import Fint.FinTribe.payload.request.*;
import Fint.FinTribe.payload.response.CommunityResponse;
import Fint.FinTribe.payload.response.VoteCheckResponse;
import Fint.FinTribe.service.community.CommunityService;
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

    //커뮤니티
    @GetMapping("/community")
    public ResponseEntity<?> getCommunityInformation(@RequestParam @Valid ObjectId artId, @RequestParam @Valid ObjectId userId){
        CommunityResponse communityResponse = communityService.getCommunity(artId, userId);
        return new ResponseEntity<>(communityResponse, HttpStatus.OK);
    }

    //게시글
    @PostMapping("/article")
    public ResponseEntity<?> postArticle(@RequestBody @Valid ArticleRequest articleRequest){
        communityService.createArticle(articleRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/revise-article")
    public ResponseEntity<?> patchArticle(@RequestBody @Valid ReviseArticleRequest articleRequest){
        communityService.updateArticle(articleRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete-article")
    public ResponseEntity<?> deleteArticle(@RequestParam @Valid ObjectId articleId){
        communityService.deleteArticle(articleId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //댓글
    @PostMapping("/comment")
    public ResponseEntity<?> postComment(@RequestBody @Valid CommentRequest commentRequest){
        communityService.createComment(commentRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/revise-comment")
    public ResponseEntity<?> patchComment(@RequestBody @Valid ReviseCommentRequest commentRequest){
        communityService.updateComment(commentRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete-comment")
    public ResponseEntity<?> deleteComment(@RequestParam @Valid ObjectId articleId, @RequestParam @Valid Integer commentId, @RequestParam @Valid Integer tagCommentId){
        communityService.deleteComment(articleId, commentId, tagCommentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //투표
    @PostMapping("/vote-proposal")
    public ResponseEntity<?> postVote(@RequestBody @Valid VoteProposalRequest voteRequest){
        communityService.createVote(voteRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/vote")
    public ResponseEntity<?> participateVote(@RequestBody @Valid VoteRequest voteRequest){
        communityService.participateVote(voteRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/check")
    public ResponseEntity<?> getVote(@RequestParam @Valid ObjectId voteId){
        VoteCheckResponse voteResponse = communityService.getVoteInformation(voteId);
        return new ResponseEntity<>(voteResponse, HttpStatus.OK);
    }
}

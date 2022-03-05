package Fint.FinTribe.controller;

import Fint.FinTribe.domain.community.Community;
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
import java.util.List;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class CommunityController {
    private final CommunityService communityService;

    //커뮤니티
    @GetMapping("/community")
    public ResponseEntity<?> getCommunity(@RequestParam @Valid ObjectId artId, @RequestParam @Valid ObjectId userId){
        CommunityResponse communityResponse = communityService.getCommunityInformation(artId, userId);
        return new ResponseEntity<>(communityResponse, HttpStatus.OK);
    }

    //게시글
    @PostMapping("/article")
    public ResponseEntity<?> createArticle(@RequestBody @Valid ArticleRequest articleRequest){
        communityService.createArticle(articleRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/revise-article")
    public ResponseEntity<?> patchArticle(@RequestBody @Valid ReviseArticleRequest articleRequest){
        communityService.patchArticle(articleRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete-article")
    public ResponseEntity<?> deleteArticle(@RequestParam @Valid Long articleId, @RequestParam @Valid ObjectId communityId){
        communityService.deleteArticle(articleId, communityId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //댓글
    @PostMapping("/comment")
    public ResponseEntity<?> createComment(@RequestBody @Valid CommentRequest commentRequest){
        communityService.createComment(commentRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/revise-comment")
    public ResponseEntity<?> patchComment(@RequestBody @Valid ReviseCommentRequest commentRequest){
        communityService.patchComment(commentRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete-comment")
    public ResponseEntity<?> deleteComment(@RequestParam @Valid Long articleId, @RequestParam @Valid List<Integer> commentId, @RequestParam @Valid ObjectId communityId, @RequestParam @Valid List<Integer> tagCommentId){
        communityService.deleteComment(articleId, commentId, communityId, tagCommentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //투표 생성
    @PostMapping("/vote-proposal")
    public ResponseEntity<?> createVote(@RequestBody @Valid VoteProposalRequest voteRequest){
        communityService.createVote(voteRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //투표 참여
    @PostMapping("/vote")
    public ResponseEntity<?> participateVote(@RequestBody @Valid VoteRequest voteRequest){
        String result = communityService.participateVote(voteRequest);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    //투표창 접속
    @GetMapping("/check")
    public ResponseEntity<?> getVote(@RequestParam @Valid ObjectId communityId, @RequestParam @Valid Integer voteId){
        VoteCheckResponse voteResponse = communityService.getVoteInformation(communityId, voteId);
        return new ResponseEntity<>(voteResponse, HttpStatus.OK);
    }
}

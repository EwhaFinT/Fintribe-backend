package Fint.FinTribe.controller;

import Fint.FinTribe.payload.request.ArticleRequest;
import Fint.FinTribe.payload.request.ReviseArticleRequest;
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

    @GetMapping("/community")
    public ResponseEntity<?> getCommunity(@RequestParam @Valid ObjectId artId, @RequestParam @Valid ObjectId userId){
        return new ResponseEntity<>(communityService.getCommunityInformation(artId, userId), HttpStatus.OK);
    }

    @PostMapping("/article")
    public ResponseEntity<?> createArticle(@RequestBody @Valid ArticleRequest articleRequest){
        communityService.createArticle(articleRequest.getUserId(), articleRequest.getTitle(), articleRequest.getContent(), articleRequest.getCommunityId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/revise-article")
    public ResponseEntity<?> patchArticle(@RequestBody @Valid ReviseArticleRequest articleRequest){
        communityService.patchArticle(articleRequest.getArticleId(), articleRequest.getTitle(), articleRequest.getContent(), articleRequest.getCommunityId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete-article")
    public ResponseEntity<?> deleteArticle(@RequestParam @Valid Long articleId, @RequestParam @Valid ObjectId communityId){
        communityService.deleteArticle(articleId, communityId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

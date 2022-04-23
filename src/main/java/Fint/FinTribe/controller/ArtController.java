package Fint.FinTribe.controller;

import Fint.FinTribe.domain.art.Art;
import Fint.FinTribe.payload.request.UploadRequest;
import Fint.FinTribe.payload.response.ArtListResponse;
import Fint.FinTribe.payload.response.UploadResponse;
import Fint.FinTribe.service.ArtService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class ArtController {
    private final ArtService artService;
    // 1. 작품 업로드
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@Valid @RequestBody UploadRequest uploadRequest) {
        UploadResponse uploadResponse = artService.upload(uploadRequest);
        return new ResponseEntity<>(uploadResponse, HttpStatus.OK);
    }

    // 2. 현재 진행중인 경매의 art url 반환 (artId, paint 리스트)
    @GetMapping("/artlist")
    public ResponseEntity<?> getArtList() {
        ArtListResponse artListResponse = artService.getArtList();
        return new ResponseEntity<>(artListResponse, HttpStatus.OK);
    }

    // 3. art 상세 정보 반환
    @GetMapping("/artInfo")
    public ResponseEntity<?> getArtInfo(@Valid @RequestParam ObjectId artId) {
        Art art = artService.getArtInfo(artId);
        return new ResponseEntity<>(art, HttpStatus.OK);
    }
}

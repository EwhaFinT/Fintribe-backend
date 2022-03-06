package Fint.FinTribe.controller;

import Fint.FinTribe.payload.request.UploadRequest;
import Fint.FinTribe.payload.response.UploadResponse;
import Fint.FinTribe.service.ArtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

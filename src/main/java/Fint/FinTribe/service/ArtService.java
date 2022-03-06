package Fint.FinTribe.service;

import Fint.FinTribe.domain.art.Art;
import Fint.FinTribe.domain.art.ArtRepository;
import Fint.FinTribe.payload.request.UploadRequest;
import Fint.FinTribe.payload.response.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArtService {
    private final ArtRepository artRepository;

    private UploadResponse upload(UploadRequest uploadRequest) {
        // ==== nft 주소 받기 ====
        // ==== 이미지 서버 등록 ====
        Art art = Art.builder()
                .artId(new ObjectId())
                .artName(uploadRequest.getArtName())
                .painter(uploadRequest.getPainter())
                .price(uploadRequest.getPrice())
                .nftAdd(null).paint(null)
                .detail(uploadRequest.getDetail()).build();
        artRepository.save(art);
        return new UploadResponse(art.getPaint());
    }
}

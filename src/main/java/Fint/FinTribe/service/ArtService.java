package Fint.FinTribe.service;

import Fint.FinTribe.domain.art.Art;
import Fint.FinTribe.domain.art.ArtRepository;
import Fint.FinTribe.payload.request.UploadRequest;
import Fint.FinTribe.payload.response.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ArtService {
    private final ArtRepository artRepository;

    private final AuctionService auctionService;

    public UploadResponse upload(UploadRequest uploadRequest) {
        // ==== nft 주소 받기 ====
        // ==== 이미지 서버 등록 ====
        List<ObjectId> userId = new ArrayList<>();
        userId.add(uploadRequest.getUserId());
        Art art = Art.builder()
                .artId(new ObjectId())
                .artName(uploadRequest.getArtName())
                .painter(uploadRequest.getPainter())
                .price(uploadRequest.getPrice())
                .nftAdd(null).paint(null).userId(userId)
                .detail(uploadRequest.getDetail()).build();
        artRepository.save(art);
        auctionService.setAuctionDate(art.getArtId(), uploadRequest.getAuctionDate());
        return new UploadResponse(art.getPaint());
    }
}

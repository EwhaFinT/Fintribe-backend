package Fint.FinTribe.service;

import Fint.FinTribe.domain.art.Art;
import Fint.FinTribe.domain.art.ArtRepository;
import Fint.FinTribe.domain.auction.Auction;
import Fint.FinTribe.payload.request.UploadRequest;
import Fint.FinTribe.payload.response.ArtListResponse;
import Fint.FinTribe.payload.response.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArtService {
    private final ArtRepository artRepository;

    private final AuctionService auctionService;

    // 1. 작품 업로드
    public UploadResponse upload(UploadRequest uploadRequest) {
        // 경매 가능한 날짜인지 확인
        if(auctionService.countArtwork(uploadRequest.getAuctionDate().toLocalDate()) >= 3) return new UploadResponse(null);
        // TODO: nft 주소 받기
        List<ObjectId> userId = new ArrayList<>();
        List<Double> ratio = new ArrayList<>();
        userId.add(new ObjectId(uploadRequest.getUserId()));
        ratio.add(1.0);
        Art art = Art.builder()
                .artId(new ObjectId()).artName(uploadRequest.getArtName()).painter(uploadRequest.getPainter())
                .price(uploadRequest.getPrice())
                .nftAdd(null)
                .paint(uploadRequest.getPaint()).sold(false)
                .userId(userId).ratio(ratio)
                .detail(uploadRequest.getDetail()).build();
        artRepository.save(art);
        auctionService.setAuctionDate(art.getArtId(), uploadRequest.getAuctionDate().toLocalDate());
        return new UploadResponse(art.getArtId().toString());
    }

    // 2. 현재 진행중인 경매의 art url 반환 (artId, paint 리스트)
    public ArtListResponse getArtList() {
        List<String> artId = new ArrayList<>();
        List<String> paint = new ArrayList<>();

        List<Auction> auctionList = auctionService.getAuctions();
        for(int i = 0; i < auctionList.size(); i++) {
            Optional<Art> art = artRepository.findById(auctionList.get(i).getArtId());
            artId.add(auctionList.get(i).getArtId().toString());
            paint.add(art.get().getPaint());
        }
        return new ArtListResponse(artId, paint);
    }

    // 3. art 상세 정보 반환
    public Art getArtInfo(ObjectId artId) {
        return artRepository.findById(artId).get();
    }

    public Art findArtById(ObjectId artId){
        //TODO; error ctrl 필요함
        return artRepository.findById(artId).orElseThrow();
    }
}

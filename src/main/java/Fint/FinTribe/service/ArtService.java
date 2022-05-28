package Fint.FinTribe.service;

import Fint.FinTribe.domain.art.Art;
import Fint.FinTribe.domain.art.ArtRepository;
import Fint.FinTribe.domain.auction.Auction;
import Fint.FinTribe.domain.user.User;
import Fint.FinTribe.domain.user.UserRespository;
import Fint.FinTribe.payload.request.UploadRequest;
import Fint.FinTribe.payload.response.ArtListResponse;
import Fint.FinTribe.payload.response.NodeResponse;
import Fint.FinTribe.payload.response.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ArtService {
    private final ArtRepository artRepository;
    private final UserRespository userRespository;

    private final AuctionService auctionService;

    // 1. 작품 업로드
    public UploadResponse upload(UploadRequest uploadRequest) {
        Optional<User> user = userRespository.findById(new ObjectId(uploadRequest.getUserId()));
        if(!user.isPresent()) return new UploadResponse(null, "접근 권한이 없습니다.");

        // 경매 가능한 날짜인지 확인
        int cnt = auctionService.countArtwork(uploadRequest.getAuctionDate().toLocalDate());
        if(cnt >= 3) return new UploadResponse(null, "해당 경매 일자에 작품을 올릴 수 없습니다.");

        // TODO: nft 주소 받기
        String txHash = getHash(uploadRequest.getPaint());

        ObjectId newArtId = new ObjectId();

        // 사용자 작품 업데이트
        List<ObjectId> artId = user.get().getArtId();
        if(artId == null) artId = new ArrayList<>();
        artId.add(newArtId);
        user.get().setArtId(artId);
        userRespository.save(user.get());

        // 작품 사용자 업데이트
        List<ObjectId> userId = new ArrayList<>();
        List<Double> ratio = new ArrayList<>();
        userId.add(new ObjectId(uploadRequest.getUserId()));
        ratio.add(1.0);
        Art art = Art.builder()
                .artId(newArtId).artName(uploadRequest.getArtName()).painter(uploadRequest.getPainter())
                .price(uploadRequest.getPrice())
                .nftAdd(txHash)
                .paint(uploadRequest.getPaint()).sold(false)
                .userId(userId).ratio(ratio)
                .detail(uploadRequest.getDetail()).build();
        artRepository.save(art);
        auctionService.setAuctionDate(art.getArtId(), uploadRequest.getAuctionDate().toLocalDate());
        return new UploadResponse(art.getArtId().toString(), null);
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

    public String getHash(String img){
        final String url = "https://fintribenode.herokuapp.com/v1/mint";
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        Map map = new HashMap<String, String>();
        map.put("Content-Type", "application/json");

        headers.setAll(map);

        Map req_payload = new HashMap();
        req_payload.put("img", img);

        HttpEntity<?> request = new HttpEntity<>(req_payload, headers);

        ResponseEntity<NodeResponse> response = new RestTemplate().postForEntity(url, request, NodeResponse.class);
        return response.getBody().getTransactionHash();
    }
}

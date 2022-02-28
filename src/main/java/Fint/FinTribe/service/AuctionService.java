package Fint.FinTribe.service;

import Fint.FinTribe.domain.art.Art;
import Fint.FinTribe.domain.art.ArtRepository;
import Fint.FinTribe.domain.auction.*;
import Fint.FinTribe.payload.request.NewPriceRequest;
import Fint.FinTribe.payload.request.ParticipateAuctionRequest;
import Fint.FinTribe.payload.request.PricelistRequest;
import Fint.FinTribe.payload.response.NewPriceResponse;
import Fint.FinTribe.payload.response.ParticipateAuctionResponse;
import Fint.FinTribe.payload.response.PricelistResponse;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final ParticipantAuctionRepository participantAuctionRepository;
    private final PriceRepository priceRepository;
    //private final UserRespository userRespository;
    private final ArtRepository artRepository;

    // 1. 새로운 경매 가격 제안
    public NewPriceResponse newPrice(NewPriceRequest newPriceRequest) {
        // 시작 경매가 확인 & 현재 상한가 확인
        PricelistResponse pricelistResponse = getPricelist(new PricelistRequest(newPriceRequest.getAuctionId()));
        if(pricelistResponse.getPrice() > newPriceRequest.getAuctionPrice()) return new NewPriceResponse(0);
        // ==== 잔액 검사 ====

        // 가격 저장
        Object newPriceId = savePrice(newPriceRequest.getAuctionId(), newPriceRequest.getAuctionPrice(), newPriceRequest.getRatio());
        saveParticipantAuction(newPriceId, newPriceRequest.getUserId(), newPriceRequest.getRatio());
        // ==== 낙찰 여부 확인 ====

        return new NewPriceResponse(newPriceId);
    }

    // 2. 기존 경매 참여
    public ParticipateAuctionResponse participateAuction(ParticipateAuctionRequest participateAuctionRequest) {
        Optional<Price> price = findPriceByPriceId(participateAuctionRequest.getPriceId());
        if(price.isEmpty()) return new ParticipateAuctionResponse(0);
        // ==== 잔액 검사 ====

        // 가격 검사
        double remainderRatio = price.get().getRemainderRatio();
        double newRatio = participateAuctionRequest.getRatio();
        if(remainderRatio < newRatio) return new ParticipateAuctionResponse(0);
        // 가격 저장 및 갱신
        Optional<ParticipantAuction> participantAuction = findParticipantAuctionByUserId(participateAuctionRequest.getUserId());
        if(participantAuction.isPresent()) updateParticipantAuction(newRatio, participantAuction.get());
        else saveParticipantAuction(price.get().getPriceId(), participateAuctionRequest.getUserId(), participateAuctionRequest.getRatio());
        updatePrice(remainderRatio-newRatio, price.get());
        // ==== 낙찰 여부 확인 ====

        return new ParticipateAuctionResponse(price.get().getPriceId());
    }

    // 3. 현재 상한가 & 기존 경매 제안 리스트 받아오기
    public PricelistResponse getPricelist(PricelistRequest pricelistRequest) {
        List<Price> pricelist = findPricelist(pricelistRequest.getAuctionId());
        Collections.sort(pricelist, new PriceComparator());

        // 1. 현재 상한가가 없는 경우 경매 시작가를 상한가로 반환
        if(pricelist == null || !existMaxPrice(pricelist)) {
            Optional<Auction> auction = findAuctionByAuctionId(pricelistRequest.getAuctionId());
            Optional<Art> art = artRepository.findById(auction.get().getArtId());
            return new PricelistResponse(art.get().getPrice(), pricelist);
        }
        // 2. 현재 상한가와 기존 경매 제안 리스트 반환
        double maxPrice = findMaxPrice(pricelist);
        return new PricelistResponse(maxPrice, pricelist);
    }

    // 3-1. 기존 경매 제안 리스트 받아오기
    private List<Price> findPricelist(Object auctionId) {
        return priceRepository.findByAuctionId(auctionId);
    }

    // 3-2. 현재 상한가 구하기
    private double findMaxPrice(List<Price> pricelist) {
        int maxIndex;
        for(maxIndex = 0; maxIndex < pricelist.size(); maxIndex++) {
            if(pricelist.get(maxIndex).getRemainderRatio() == 0)
                break;
        }
        return pricelist.get(maxIndex).getAuctionPrice();
    }

    private Optional<Auction> findAuctionByAuctionId(Object auctionId) { return auctionRepository.findById(auctionId); }

    private Optional<Price> findPriceByPriceId(Object priceId) { // 가격 아이디로 가격 조회
        return priceRepository.findById(priceId);
    }

    private Optional<ParticipantAuction> findParticipantAuctionByUserId(Object userId) { return participantAuctionRepository.findByUserId(userId); }

    private Object savePrice(Object auctionId, double auctionPrice, double remainderRatio) {
        ObjectId newPriceId = new ObjectId();
        Price price = Price.builder()
                .priceId(newPriceId)
                .auctionId(auctionId).auctionPrice(auctionPrice)
                .remainderRatio(1.0 - remainderRatio).build();
        priceRepository.save(price);
        return newPriceId;
    }

    private Object saveParticipantAuction(Object priceId, Object userId, double ratio) {
        ParticipantAuction participantAuction = ParticipantAuction.builder()
                .participantAuctionId(new ObjectId())
                .priceId(priceId)
                .userId(userId)
                .ratio(ratio).build();
        return participantAuctionRepository.save(participantAuction);
    }

    private Object updatePrice(double newRatio, Price price) {
        price.setRemainderRatio(newRatio);
        return priceRepository.save(price);
    }

    private Object updateParticipantAuction(double newRatio, ParticipantAuction participantAuction) {
        double originalRatio = participantAuction.getRatio();
        participantAuction.setRatio(originalRatio + newRatio);
        return participantAuctionRepository.save(participantAuction);
    }

    // 잔액 조회
    private boolean checkRemainder() {
        return true;
    }

    private boolean existMaxPrice(List<Price> pricelist) {
        for(int i = 0; i < pricelist.size(); i++) {
            if(pricelist.get(i).getRemainderRatio() == 0) return true;
        }
        return false;
    }

    public void deleteAll() { // (단위 테스트용)
        auctionRepository.deleteAll();
        participantAuctionRepository.deleteAll();
        priceRepository.deleteAll();
    }

    public void createSampleAuction() { // (단위 테스트용)
        auctionRepository.save(
                Auction.builder()
                        .auctionId("auctionId").artId("artId").build());
    }
}

class PriceComparator implements Comparator<Price> { // 가격 내림차순 정렬
    @Override
    public int compare(Price p1, Price p2) {
        if(p1.getAuctionPrice() > p2.getAuctionPrice()) return -1;
        else if(p1.getAuctionPrice() < p2.getAuctionPrice()) return 1;
        return 0;
    }
}

package Fint.FinTribe.service;

import Fint.FinTribe.domain.art.Art;
import Fint.FinTribe.domain.art.ArtRepository;
import Fint.FinTribe.domain.auction.*;
import Fint.FinTribe.domain.auctionDate.AuctionDate;
import Fint.FinTribe.domain.auctionDate.AuctionDateRepository;
import Fint.FinTribe.domain.user.UserRespository;
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

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final ParticipantAuctionRepository participantAuctionRepository;
    private final PriceRepository priceRepository;
    private final UserRespository userRespository;
    private final ArtRepository artRepository;
    private final AuctionDateRepository auctionDateRepository;

    public HashMap<ObjectId, Price> auctionMap = new HashMap<ObjectId, Price>();

    // 1. 새로운 경매 가격 제안
    public NewPriceResponse newPrice(NewPriceRequest newPriceRequest) {
        // 시작 경매가 확인 & 현재 상한가 확인
        PricelistResponse pricelistResponse = getPricelist(new PricelistRequest(newPriceRequest.getAuctionId()));
        if(pricelistResponse.getPrice() > newPriceRequest.getAuctionPrice()) return new NewPriceResponse(null);
        // ==== 잔액 검사 ====

        // 가격 저장
        ObjectId newPriceId = (ObjectId) savePrice(newPriceRequest.getAuctionId(), newPriceRequest.getAuctionPrice(), newPriceRequest.getRatio());
        saveParticipantAuction(newPriceId, newPriceRequest.getUserId(), newPriceRequest.getRatio());
        // 낙찰인 경우
        if(newPriceRequest.getRatio() == 1.0) successAuction(newPriceId);
        return new NewPriceResponse(newPriceId);
    }

    // 2. 기존 경매 참여
    public ParticipateAuctionResponse participateAuction(ParticipateAuctionRequest participateAuctionRequest) {
        Optional<Price> price = findPriceByPriceId(participateAuctionRequest.getPriceId());
        // ==== 잔액 검사 ====

        // 가격 검사
        double remainderRatio = price.get().getRemainderRatio();
        double newRatio = participateAuctionRequest.getRatio();
        if(remainderRatio < newRatio) return new ParticipateAuctionResponse(null);
        // 가격 저장 및 갱신
        Optional<ParticipantAuction> participantAuction = findParticipantAuctionByUserId(participateAuctionRequest.getUserId());
        if(participantAuction.isPresent()) updateParticipantAuction(newRatio, participantAuction.get());
        else saveParticipantAuction(price.get().getPriceId(), participateAuctionRequest.getUserId(), participateAuctionRequest.getRatio());
        updatePrice(remainderRatio-newRatio, price.get());
        // 낙찰인 경우
        if(participateAuctionRequest.getRatio() == 1.0) successAuction(price.get().getPriceId());
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
    private List<Price> findPricelist(ObjectId auctionId) {
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

    private Optional<Auction> findAuctionByAuctionId(ObjectId auctionId) { return auctionRepository.findById(auctionId); }

    private Optional<Price> findPriceByPriceId(ObjectId priceId) { // 가격 아이디로 가격 조회
        return priceRepository.findById(priceId);
    }

    private Optional<ParticipantAuction> findParticipantAuctionByUserId(ObjectId userId) { return participantAuctionRepository.findByUserId(userId); }

    private Object savePrice(ObjectId auctionId, double auctionPrice, double remainderRatio) {
        ObjectId newPriceId = new ObjectId();
        Price price = Price.builder()
                .priceId(newPriceId)
                .auctionId(auctionId).auctionPrice(auctionPrice)
                .remainderRatio(1.0 - remainderRatio).build();
        priceRepository.save(price);
        return newPriceId;
    }

    private Object saveParticipantAuction(ObjectId priceId, ObjectId userId, double ratio) {
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

    // ==== 잔액 조회 ====
    private boolean checkRemainder(ObjectId userId) {
        return true;
    }

    // 낙찰 성공시 map에 추가
    private void successAuction(ObjectId priceId) {
        Optional<Price> price = priceRepository.findById(priceId);
        if(auctionMap.containsKey(price.get().getPriceId())) {
            Price maxPrice = auctionMap.get(priceId);
            if(maxPrice.getAuctionPrice() < price.get().getAuctionPrice()) {
                auctionMap.replace(priceId, price.get());
            }
        }
        else auctionMap.put(priceId, price.get());
    }

    private boolean existMaxPrice(List<Price> pricelist) {
        for(int i = 0; i < pricelist.size(); i++) {
            if(pricelist.get(i).getRemainderRatio() == 0) return true;
        }
        return false;
    }

    // 낙찰 결제 진행
    public void makePayment() {
        Iterator<ObjectId> keys = auctionMap.keySet().iterator();
        while(keys.hasNext()) {
            ObjectId priceId = keys.next();
            double price = auctionMap.get(priceId).getAuctionPrice();
            List<ParticipantAuction> participantAuctionList = participantAuctionRepository.findByPriceId(priceId);
            for(int i = 0; i < participantAuctionList.size(); i++) {
                double individualPrice = price * participantAuctionList.get(i).getRatio();
                // ==== 결제 진행 ====
            }
        }
    }

    public void saveAuctionDate(ObjectId artId, LocalDateTime date) {
        Optional<AuctionDate> auctionDate = auctionDateRepository.findByAuctionDate(date);
        // 해당 date가 auctionDate 테이블에 있는 경우 update
        if(auctionDate.isPresent()) updateAuctionDate(artId, auctionDate.get());
        // 해당 date가 auctionDate 테이블에 없는 경우 save
        else saveAuctionDate(artId, date);
    }

    private Object saveAuctionDate(LocalDateTime date, ObjectId artId) {
        List<ObjectId> artIdList = new ArrayList<>();
        artIdList.add(artId);
        AuctionDate newAuctionDate = AuctionDate.builder()
                .auctionDateId(new ObjectId())
                .auctionDate(date).artId(artIdList).build();
        return auctionDateRepository.save(newAuctionDate);
    }

    private Object updateAuctionDate(ObjectId artId, AuctionDate auctionDate) {
        List<ObjectId> artIdList = auctionDate.getArtId();
        artIdList.add(artId);
        auctionDate.setArtId(artIdList);
        return auctionDateRepository.save(auctionDate);
    }

    // (단위 테스트용, 추후 삭제)
    public void deleteAll() { // (단위 테스트용)
        auctionRepository.deleteAll();
        participantAuctionRepository.deleteAll();
        priceRepository.deleteAll();
    }
    public void createSampleAuction() { // (단위 테스트용)
        auctionRepository.save(
                Auction.builder()
                        .auctionId(new ObjectId()).artId(new ObjectId()).build());
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
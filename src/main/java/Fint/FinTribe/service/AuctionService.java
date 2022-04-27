package Fint.FinTribe.service;

import Fint.FinTribe.domain.art.Art;
import Fint.FinTribe.domain.art.ArtRepository;
import Fint.FinTribe.domain.auction.*;
import Fint.FinTribe.domain.auctionDate.AuctionDate;
import Fint.FinTribe.domain.auctionDate.AuctionDateRepository;
import Fint.FinTribe.domain.resaleDate.ResaleDate;
import Fint.FinTribe.domain.resaleDate.ResaleDateRepository;
import Fint.FinTribe.domain.user.User;
import Fint.FinTribe.payload.request.*;
import Fint.FinTribe.payload.response.*;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import xyz.groundx.caver_ext_kas.CaverExtKAS;
import xyz.groundx.caver_ext_kas.rest_client.io.swagger.client.ApiException;
import xyz.groundx.caver_ext_kas.rest_client.io.swagger.client.api.wallet.model.ProcessRLPRequest;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final ParticipantAuctionRepository participantAuctionRepository;
    private final PriceRepository priceRepository;
    private final AuctionDateRepository auctionDateRepository;
    private final ResaleDateRepository resaleDateRepository;
    private final ArtRepository artRepository;

    private final UserService userService;

    public HashMap<ObjectId, Price> auctionMap = new HashMap<ObjectId, Price>();

    // 1-1. 새로운 경매 가격 제안 (거래 요청)
    public TransactionResponse newPrice(NewPriceTransactionRequest newPriceTransactionRequest) {
        // 시작 경매가 확인 & 현재 상한가 확인
        PricelistResponse pricelistResponse = getPricelist(newPriceTransactionRequest.getAuctionId());
        if(pricelistResponse.getPrice() > newPriceTransactionRequest.getAuctionPrice()) return new TransactionResponse(0, null, null);
        // 거래 진행 (프론트에서 submit == false 상태로 제출)
        return makeTransaction(newPriceTransactionRequest.getAuctionId(), newPriceTransactionRequest.getAuctionPrice(), newPriceTransactionRequest.getRatio());
    }
    // 1-2. 새로운 경매 가격 제안 (거래 성사)
    public NewPriceResponse newPriceSuccess(NewPriceRequest newPriceRequest) {
        ObjectId auctionId = auctionRepository.findByArtId(new ObjectId(newPriceRequest.getArtId())).get().getAuctionId();
        // 가격 저장
        ObjectId newPriceId = (ObjectId) savePrice(auctionId, newPriceRequest.getAuctionPrice(), newPriceRequest.getRatio());
        saveParticipantAuction(newPriceId, new ObjectId(newPriceRequest.getUserId()), newPriceRequest.getRatio(), newPriceRequest.getRlp());
        // 낙찰인 경우
        if(newPriceRequest.getRatio() == 1.0) successAuction(newPriceId);
        return new NewPriceResponse(newPriceId.toString());
    }

    // 2-1. 기존 경매 참여 (거래 요청)
    public TransactionResponse participateAuction(ParticipateAuctionTransactionRequest participateAuctionTransactionRequest) {
        Optional<Price> price = findPriceByPriceId(participateAuctionTransactionRequest.getPriceId());
        // 가격 검사
        double remainderRatio = price.get().getRemainderRatio();
        double newRatio = participateAuctionTransactionRequest.getRatio();
        if(remainderRatio < newRatio) return new TransactionResponse(0, null, null);
        // 거래 진행
        return makeTransaction(price.get().getAuctionId(), price.get().getAuctionPrice(), newRatio);
    }
    // 2-2. 기존 경매 참여 (거래 성사)
    public ParticipateAuctionResponse participateAuctionSuccess(ParticipateAuctionRequest participateAuctionRequest) {
        Optional<Price> price = findPriceByPriceId(new ObjectId(participateAuctionRequest.getPriceId()));
        // 가격 저장 및 갱신
        double remainderRatio = price.get().getRemainderRatio();
        double newRatio = participateAuctionRequest.getRatio();
        if(remainderRatio < newRatio) return new ParticipateAuctionResponse("");
        Optional<ParticipantAuction> participantAuction = findParticipantAuctionByUserIdAndPriceId(new ObjectId(participateAuctionRequest.getUserId()), new ObjectId(participateAuctionRequest.getPriceId()));

        if(participantAuction.isPresent()) updateParticipantAuction(newRatio, participantAuction.get());
        else saveParticipantAuction(price.get().getPriceId(), new ObjectId(participateAuctionRequest.getUserId()), participateAuctionRequest.getRatio(), participateAuctionRequest.getRlp());
        updatePrice(remainderRatio-newRatio, price.get());
        // 낙찰인 경우
        if(participateAuctionRequest.getRatio() == 1.0) successAuction(price.get().getPriceId());
        return new ParticipateAuctionResponse(price.get().getPriceId().toString());
    }

    // 거래 생성
    private TransactionResponse makeTransaction(ObjectId auctionId, double price, double ratio) {
        Optional<Auction> auction = auctionRepository.findById(auctionId);
        if(auction.isPresent()) {
            Optional<Art> art = artRepository.findById(auction.get().getArtId());
            double gas = 0; // TODO: 가스비 수정 필요
            List<String> to = new ArrayList<>(); // 판매자 리스트
            List<Double> value = new ArrayList<>(); // 판매자별 지분 리스트

            List<ObjectId> userList = art.get().getUserId();
            List<Double> ratioList = art.get().getRatio();
            for(int i = 0; i < userList.size(); i++) {
                Optional<User> user = userService.findByUserId(userList.get(i));
                to.add(user.get().getWallet());
                value.add(price * ratio * ratioList.get(i));
            }
            return new TransactionResponse(gas, to, value);
        }
        return new TransactionResponse(0, null, null);
    }

    // 3. 현재 상한가 & 기존 경매 제안 리스트 받아오기
    public PricelistResponse getPricelist(ObjectId artId) {
        ObjectId auctionId = auctionRepository.findByArtId(artId).get().getAuctionId();

        List<PriceResponse> pricelist = findPricelist(auctionId);
        Collections.sort(pricelist, new PriceComparator());

        double maxPrice = findMaxPrice(pricelist);
        // 1. 현재 상한가가 없는 경우 경매 시작가를 상한가로 반환
        if(pricelist == null || maxPrice == -1) {
            Optional<Auction> auction = findAuctionByAuctionId(auctionId);
            Optional<Art> art = artRepository.findById(auction.get().getArtId());
            return new PricelistResponse(art.get().getPrice(), pricelist);
        }
        // 2. 현재 상한가와 기존 경매 제안 리스트 반환
        return new PricelistResponse(maxPrice, pricelist);
    }

    // 3-1. 기존 경매 제안 리스트 받아오기
    private List<PriceResponse> findPricelist(ObjectId auctionId) {
        List<Price> pricelist = priceRepository.findByAuctionId(auctionId);
        List<PriceResponse> str_pricelist = new ArrayList<>();

        for(int i = 0; i < pricelist.size(); i++) {
            Price tmp = pricelist.get(i);
            str_pricelist.add(new PriceResponse(tmp.getPriceId().toString(), tmp.getAuctionId().toString(), tmp.getAuctionPrice(), tmp.getRemainderRatio()));
        }
        return str_pricelist;
    }

    // 3-2. 현재 상한가 구하기
    private double findMaxPrice(List<PriceResponse> pricelist) {
        int maxIndex;
        for(maxIndex = 0; maxIndex < pricelist.size(); maxIndex++) {
            if(pricelist.get(maxIndex).getRemainderRatio() == 0)
                break;
        }
        if(maxIndex == pricelist.size()) return -1; // 현재 상한가가 없는 경우
        return pricelist.get(maxIndex).getAuctionPrice();
    }

    private Optional<Auction> findAuctionByAuctionId(ObjectId auctionId) { return auctionRepository.findById(auctionId); }
    private Optional<Price> findPriceByPriceId(ObjectId priceId) { return priceRepository.findById(priceId); }
    private Optional<ParticipantAuction> findParticipantAuctionByUserIdAndPriceId(ObjectId userId, ObjectId priceId) { return participantAuctionRepository.findByUserIdAndPriceId(userId, priceId); }

    private Object savePrice(ObjectId auctionId, double auctionPrice, double remainderRatio) {
        ObjectId newPriceId = new ObjectId();
        Price price = Price.builder()
                .priceId(newPriceId)
                .auctionId(auctionId).auctionPrice(auctionPrice)
                .remainderRatio(1.0 - remainderRatio).build();
        priceRepository.save(price);
        return newPriceId;
    }

    private Object saveParticipantAuction(ObjectId priceId, ObjectId userId, double ratio, List<String> transactionHash) {
        ParticipantAuction participantAuction = ParticipantAuction.builder()
                .participantAuctionId(new ObjectId()).priceId(priceId).userId(userId)
                .ratio(ratio).rlp(transactionHash).build();
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

    // 현재 진행중인 경매 정보 반환
    public List<Auction> getAuctions() { return auctionRepository.findByIsDeleted(false); }

    // 경매 종료 (논리 삭제)
    public void deleteAuctions() {
        List<Auction> auctionList = getAuctions();
        for(int i = 0; i < auctionList.size(); i++) deleteAuction(auctionList.get(i));
    }

    private Object deleteAuction(Auction auction) {
        auction.setDeleted(true);
        return auctionRepository.save(auction);
    }

    // 경매 생성
    public void makeAuctions (LocalDate date) {
        // 경매 조회
        Optional<AuctionDate> auctionDate = auctionDateRepository.findByAuctionDate(date);
        if(auctionDate.isPresent()) {
            List<ObjectId> artIdList = auctionDate.get().getArtId();
            for(int i = 0; i < artIdList.size(); i++) saveAuction(artIdList.get(i));
        }
        // 재경매 조회
        Optional<ResaleDate> resaleDate = resaleDateRepository.findByResaleDate(date);
        if(resaleDate.isPresent()) {
            List<ObjectId> artIdList = resaleDate.get().getArtId();
            for(int i = 0; i < artIdList.size(); i++) saveAuction(artIdList.get(i));
        }
    }

    private Object saveAuction(ObjectId artId) {
        Auction auction = Auction.builder()
                .auctionId(new ObjectId()).artId(artId).isDeleted(false).build();
        return auctionRepository.save(auction);
    }

    // 낙찰 결제 진행 및 판매 상태 변경
    public void makePayment() throws ApiException {
        Iterator<ObjectId> keys = auctionMap.keySet().iterator();
        while(keys.hasNext()) {
            ObjectId priceId = keys.next();
            List<ParticipantAuction> participantAuctionList = participantAuctionRepository.findByPriceId(priceId);

            List<ObjectId> participantUserId = new ArrayList<>();
            List<Double> participantRatio = new ArrayList<>();
            List<String> rlp = new ArrayList<>();

            for(int i = 0; i < participantAuctionList.size(); i++) {
                participantUserId.add(participantAuctionList.get(i).getUserId());
                participantRatio.add(participantAuctionList.get(i).getRatio());
                List<String> participantRlp = participantAuctionList.get(i).getRlp();
                for(int j = 0; j < participantRlp.size(); j++) rlp.add(participantRlp.get(j));
            }

            // 판매 상태 변경
            ObjectId auctionId = auctionMap.get(priceId).getAuctionId();
            Optional<Auction> auction = auctionRepository.findById(auctionId);
            changeSoldState(auction.get().getArtId(), participantUserId, participantRatio);

            // 결제 승인
            for(int i = 0; i < rlp.size(); i++) {
                // klaytn 대납 기능을 통해 거래 submit
                submitRlpTransaction(rlp.get(i));
            }
        }
        auctionMap.clear();
    }

    private void submitRlpTransaction(String rlp) throws ApiException {
        CaverExtKAS caver = new CaverExtKAS();
        ProcessRLPRequest requestRLP = new ProcessRLPRequest();
        requestRLP.setRlp(rlp);
        requestRLP.setSubmit(true);
        caver.kas.wallet.requestRawTransaction(requestRLP);
    }

    private void changeSoldState(ObjectId artId, List<ObjectId> userId, List<Double> ratio) { // 판매 상태 변경
        Optional<Art> art = artRepository.findById(artId);
        if(art.isPresent()) updateArt(art.get(), userId, ratio);
    }

    private Object updateArt(Art art, List<ObjectId> userId, List<Double> ratio) {
        art.setSold(true);
        art.setUserId(userId);
        art.setRatio(ratio);
        return artRepository.save(art);
    }

    public void setAuctionDate(ObjectId artId, LocalDate date) { // (작품 업로드)
        Optional<AuctionDate> auctionDate = auctionDateRepository.findByAuctionDate(date);
        if(auctionDate.isPresent()) updateAuctionDate(artId, auctionDate.get());
        else saveAuctionDate(date, artId);
    }

    private Object saveAuctionDate(LocalDate date, ObjectId artId) {
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
}

class PriceComparator implements Comparator<PriceResponse> { // 가격 내림차순 정렬
    @Override
    public int compare(PriceResponse p1, PriceResponse p2) {
        if(p1.getAuctionPrice() > p2.getAuctionPrice()) return -1;
        else if(p1.getAuctionPrice() < p2.getAuctionPrice()) return 1;
        return 0;
    }
}
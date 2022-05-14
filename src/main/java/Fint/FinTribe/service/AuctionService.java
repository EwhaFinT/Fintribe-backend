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
import Fint.FinTribe.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import xyz.groundx.caver_ext_kas.CaverExtKAS;
import xyz.groundx.caver_ext_kas.rest_client.io.swagger.client.ApiException;
import xyz.groundx.caver_ext_kas.rest_client.io.swagger.client.api.wallet.model.FDTransactionResult;
import xyz.groundx.caver_ext_kas.rest_client.io.swagger.client.api.wallet.model.FDUserProcessRLPRequest;
import xyz.groundx.caver_ext_kas.rest_client.io.swagger.client.api.wallet.model.TransactionResult;
import xyz.groundx.caver_ext_kas.rest_client.io.swagger.client.api.wallet.model.ValueTransferTransactionRequest;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionService {
    private final String chainId = "1001";
    private final String accessKeyId = "KASK489KAHY54740WDAAL1PU";
    private final String secretAccessKey = "KcCPXC2EiGze7svsh0v1w7tlnb9e-q23QoUW4yWs";
    private final String feePayer = "0xECaf89B630EE5F86bE75Bd4944DB51E3B3809a16";

    private final AuctionRepository auctionRepository;
    private final ParticipantAuctionRepository participantAuctionRepository;
    private final PriceRepository priceRepository;
    private final AuctionDateRepository auctionDateRepository;
    private final ResaleDateRepository resaleDateRepository;
    private final ArtRepository artRepository;

    private final UserService userService;
    private final CommunityService communityService;

    public HashMap<ObjectId, Price> auctionMap = new HashMap<ObjectId, Price>();

    // 1. 새로운 경매
    public NewPriceResponse newPrice(NewPriceRequest newPriceRequest) {
        ObjectId auctionId = auctionRepository.findByArtId(new ObjectId(newPriceRequest.getArtId())).get().getAuctionId();
        ObjectId userId = new ObjectId(newPriceRequest.getUserId());
        if(newPriceRequest.getRatio() > 1.0 || newPriceRequest.getRatio() <= 0.0) return new NewPriceResponse("");   // 지분 설정 오류

        ObjectId newPriceId = (ObjectId) savePrice(auctionId, newPriceRequest.getAuctionPrice(), newPriceRequest.getRatio());   // 가격 저장
        List<String> rlp = makeRLPTx(auctionId, userId, newPriceRequest.getAuctionPrice(), newPriceRequest.getRatio());         // RLP 생성
        saveParticipantAuction(newPriceId, userId, newPriceRequest.getRatio(), rlp);

        if(newPriceRequest.getRatio() == 1.0) successAuction(auctionId, newPriceId);   // 낙찰인 경우
        return new NewPriceResponse(newPriceId.toString());
    }

    // 2. 기존 경매
    public ParticipateAuctionResponse participateAuction(ParticipateAuctionRequest participateAuctionRequest) {
        Price price = findPriceByPriceId(new ObjectId(participateAuctionRequest.getPriceId())).get();
        // 가격 저장 및 갱신
        double remainderRatio = price.getRemainderRatio();
        double newRatio = participateAuctionRequest.getRatio();
        if(remainderRatio < newRatio || newRatio <= 0.0) return new ParticipateAuctionResponse("");    // 지분 설정 오류

        ObjectId userId = new ObjectId(participateAuctionRequest.getUserId());
        List<String> rlp = makeRLPTx(price.getAuctionId(), userId, price.getAuctionPrice(), newRatio);
        Optional<ParticipantAuction> participantAuction = findParticipantAuctionByUserIdAndPriceId(userId, price.getPriceId());
        if(participantAuction.isPresent()) updateParticipantAuction(newRatio, participantAuction.get());    // 지분 갱신
        else saveParticipantAuction(price.getPriceId(), userId, participateAuctionRequest.getRatio(), rlp);
        updatePrice(remainderRatio-newRatio, price);    // 가격 갱신

        if(participateAuctionRequest.getRatio() == 1.0) successAuction(price.getAuctionId(), price.getPriceId()); // 낙찰인 경우
        return new ParticipateAuctionResponse(price.getPriceId().toString());
    }

    // 거래 RLP 생성
    private List<String> makeRLPTx(ObjectId auctionId, ObjectId userId, double price, double ratio) {
        String from = userService.findByUserId(userId).get().getWallet();
        ObjectId artId = auctionRepository.findById(auctionId).get().getArtId();
        Art art = artRepository.findById(artId).get();
        List<ObjectId> objTo = art.getUserId();
        List<Double> ratioList = art.getRatio();
        double value = price * ratio;

        CaverExtKAS caver = new CaverExtKAS();
        caver.initKASAPI(chainId, accessKeyId, secretAccessKey);

        List<String> rlp = new ArrayList<>();
        for(int i = 0; i < objTo.size(); i++) {
            String to = userService.findByUserId(objTo.get(i)).get().getWallet();
            Double perRatio = ratioList.get(i);

            long dtol = new Double(value * perRatio).longValue();
            String stoh = "0x" + Long.toHexString(dtol);

            try {
                ValueTransferTransactionRequest request = new ValueTransferTransactionRequest();
                request.setFrom(from);
                request.setTo(to);
                request.setValue(stoh);
                request.setSubmit(false);

                TransactionResult transactionResult = caver.kas.wallet.requestValueTransfer(request);
                rlp.add(transactionResult.getRlp());
            } catch (ApiException e) {
                System.out.println(e.getResponseBody());
                e.printStackTrace();
            }
        }
        return rlp;
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

    // 낙찰 성공시 이전 경매 내역애서 상한가보다 낮은 경매 삭제 및 map에 추가
    private void successAuction(ObjectId auctionId, ObjectId priceId) {
        Price newPrice = priceRepository.findById(priceId).get();   // (auctionId, Price)
        if(auctionMap.containsKey(auctionId)) {
            Price maxPrice = auctionMap.get(auctionId);
            if(maxPrice.getAuctionPrice() < newPrice.getAuctionPrice()) {   // 상한가 갱신
                auctionMap.replace(auctionId, newPrice);
                deleteInvalidAuctions(auctionId, newPrice.getAuctionPrice());
            }
        }
        else auctionMap.put(auctionId, newPrice);
    }
    
    // 현재 상한가보다 낮은 경매가 삭제
    private void deleteInvalidAuctions(ObjectId auctionId, double maxPrice) {
        List<Price> pricelist = priceRepository.findByAuctionId(auctionId); // 현 경매에 해당하는 가격 조회

        List<ObjectId> invalidPriceId = new ArrayList<>();
        for(int i = 0; i < pricelist.size(); i++) { // 현재 상한가보다 낮은 가격 삭제
            Price cmp = pricelist.get(i);
            if((cmp.getAuctionPrice() < maxPrice) || (cmp.getAuctionPrice() == maxPrice && cmp.getRemainderRatio() > 0))
                invalidPriceId.add(cmp.getPriceId());
        }
        for(int i = 0; i < invalidPriceId.size(); i++) { // 현재 상한가보다 낮은 가격 삭제
            priceRepository.deleteById(invalidPriceId.get(i));
            participantAuctionRepository.deleteByPriceId(invalidPriceId.get(i));
        }
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
    public void makePayment() {
        Iterator<ObjectId> keys = auctionMap.keySet().iterator();   // (auctionId, Price)
        while(keys.hasNext()) {
            ObjectId auctionId = keys.next();
            Price price = auctionMap.get(auctionId);
            List<ParticipantAuction> participantAuctionList = participantAuctionRepository.findByPriceId(price.getPriceId());

            List<ObjectId> participantUserId = new ArrayList<>();   // 공동 투자자
            List<Double> participantRatio = new ArrayList<>();      // 공동 투자자별 지분
            List<String> rlp = new ArrayList<>();

            for(int i = 0; i < participantAuctionList.size(); i++) {
                participantUserId.add(participantAuctionList.get(i).getUserId());
                participantRatio.add(participantAuctionList.get(i).getRatio());
                List<String> participantRlp = participantAuctionList.get(i).getRlp();
                for(int j = 0; j < participantRlp.size(); j++) rlp.add(participantRlp.get(j));
            }

            // 결제 승인
            for(int i = 0; i < rlp.size(); i++) submitRlpTransaction(rlp.get(i));

            // 낙찰 알림 메일 전송
            Art art = artRepository.findByAuctionId(auctionId).get();
            for(int i = 0; i < participantUserId.size(); i++) {
                User user = userService.findByUserId(participantUserId.get(i)).get();
                userService.sendAuctionAlarm(user.getName(), art.getArtName(), user.getEmail());
            }
            
            // 판매 상태 변경 및 커뮤니티 생성
            Auction auction = auctionRepository.findById(auctionId).get();
            changeSoldState(auction.getArtId(), participantUserId, participantRatio);
            communityService.createCommunity(auction.getArtId(), participantUserId, participantRatio);
        }
        auctionMap.clear();
    }

    private void submitRlpTransaction(String rlp) {
        CaverExtKAS caver = new CaverExtKAS();
        caver.initKASAPI(chainId, accessKeyId, secretAccessKey);

        FDUserProcessRLPRequest rlpRequest = new FDUserProcessRLPRequest();
        rlpRequest.setRlp(rlp);
        rlpRequest.setFeePayer(feePayer);
        rlpRequest.setSubmit(true);

        try{
            FDTransactionResult result = caver.kas.wallet.requestFDRawTransactionPaidByUser(rlpRequest);
        } catch(ApiException e) {
            e.printStackTrace();
        }
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

    public int countArtwork(LocalDate date) {
        System.out.println(date.toString()); // TODO : 추후 삭제 (디버깅용)
        AuctionDate auctionDate = auctionDateRepository.findByAuctionDate(date).get();
        return auctionDate.getArtId().size(); // 신규 경매
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
        else if(p1.getRemainderRatio() < p2.getRemainderRatio()) return -1;
        else if(p1.getRemainderRatio() > p2.getRemainderRatio()) return 1;
        return 0;
    }
}
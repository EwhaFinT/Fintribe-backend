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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import xyz.groundx.caver_ext_kas.CaverExtKAS;
import xyz.groundx.caver_ext_kas.rest_client.io.swagger.client.ApiException;
import xyz.groundx.caver_ext_kas.rest_client.io.swagger.client.api.wallet.model.*;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionService {
    @Value("${caver.kas.chainId}")
    private String chainId;
    @Value("${caver.kas.accessKeyId}")
    private String accessKeyId;
    @Value("${caver.kas.secretAccessKey}")
    private String secretAccessKey;
    @Value("${caver.kas.feePayer}")
    private String feePayer;

    private final AuctionRepository auctionRepository;
    private final ParticipantAuctionRepository participantAuctionRepository;
    private final PriceRepository priceRepository;
    private final AuctionDateRepository auctionDateRepository;
    private final ResaleDateRepository resaleDateRepository;
    private final ArtRepository artRepository;

    private final UserService userService;
    private final CommunityService communityService;

    // 1. 새로운 경매
    public NewPriceResponse newPrice(NewPriceRequest newPriceRequest) {
        Optional<Auction> auction = auctionRepository.findByArtId(new ObjectId(newPriceRequest.getArtId()));
        if(!auction.isPresent()) return new NewPriceResponse(null, "해당 경매에 참여하실 수 없습니다.");  // 경매 정보를 찾을 수 없는 경우

        ObjectId auctionId = auction.get().getAuctionId();
        double maxPrice = findMaxPrice(auctionId);
        if(maxPrice == -1) maxPrice = getStartPrice(auctionId);
        if(newPriceRequest.getAuctionPrice() <= maxPrice) return new NewPriceResponse(null, "현재 상한가보다 높은 가격을 제시해주세요.");  // 제안가 오류

        ObjectId userId = new ObjectId(newPriceRequest.getUserId());
        if(newPriceRequest.getRatio() > 1.0 || newPriceRequest.getRatio() <= 0.0) return new NewPriceResponse(null, "올바르지 못한 지분입니다.");   // 지분 설정 오류

        ObjectId newPriceId = (ObjectId) savePrice(auctionId, newPriceRequest.getAuctionPrice(), newPriceRequest.getRatio());   // 가격 저장
        List<String> rlp = makeRLPTx(auctionId, userId, newPriceRequest.getAuctionPrice(), newPriceRequest.getRatio());         // RLP 생성
        saveParticipantAuction(newPriceId, userId, newPriceRequest.getRatio(), rlp);

        if(newPriceRequest.getRatio() == 1.0) successAuction(auctionId, newPriceId);   // 낙찰인 경우
        return new NewPriceResponse(newPriceId.toString(), null);
    }

    // 2. 기존 경매
    public ParticipateAuctionResponse participateAuction(ParticipateAuctionRequest participateAuctionRequest) {
        Price price = findPriceByPriceId(new ObjectId(participateAuctionRequest.getPriceId())).get();
        // 가격 저장 및 갱신
        double remainderRatio = price.getRemainderRatio();
        double newRatio = participateAuctionRequest.getRatio();
        if(remainderRatio < newRatio || newRatio <= 0.0) return new ParticipateAuctionResponse(null);    // 지분 설정 오류

        ObjectId userId = new ObjectId(participateAuctionRequest.getUserId());
        List<String> rlp = makeRLPTx(price.getAuctionId(), userId, price.getAuctionPrice(), newRatio);
        Optional<ParticipantAuction> participantAuction = findParticipantAuctionByUserIdAndPriceId(userId, price.getPriceId());
        if(participantAuction.isPresent()) updateParticipantAuction(newRatio, participantAuction.get());    // 지분 갱신
        else saveParticipantAuction(price.getPriceId(), userId, participateAuctionRequest.getRatio(), rlp);
        updatePrice(remainderRatio-newRatio, price);    // 가격 갱신

        if(remainderRatio + newRatio == 1.0) successAuction(price.getAuctionId(), price.getPriceId()); // 낙찰인 경우
        return new ParticipateAuctionResponse(price.getPriceId().toString());
    }

    // 거래 RLP 생성
    private List<String> makeRLPTx(ObjectId auctionId, ObjectId userId, double price, double ratio) {
        Optional<User> user = userService.findByUserId(userId);
        Optional<Auction> auction = auctionRepository.findById(auctionId);
        if(!user.isPresent() || !auction.isPresent()) return null;

        String from = user.get().getWallet();
        ObjectId artId = auction.get().getArtId();
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
                FDUserValueTransferTransactionRequest request = new FDUserValueTransferTransactionRequest();
                request.setFrom(from);
                request.setTo(to);
                request.setFeePayer(feePayer);
                request.setValue(stoh);
                request.setSubmit(false);

                FDTransactionResult transactionResult = caver.kas.wallet.requestFDValueTransferPaidByUser(request);
                rlp.add(transactionResult.getRlp());
            } catch (ApiException e) {
                System.out.println("MAKE TRANSACTION ERROR");
                System.out.println(e.getResponseBody());
            }
        }
        return rlp;
    }

    // 3. 현재 상한가 & 기존 경매 제안 리스트 받아오기
    public PricelistResponse getPricelist(ObjectId artId) {
        Optional<Auction> checkAuction = auctionRepository.findByArtId(artId);
        if(!checkAuction.isPresent()) return new PricelistResponse(0, null);    // 경매 정보를 찾을 수 없는 경우

        ObjectId auctionId = checkAuction.get().getAuctionId();
        List<PriceResponse> pricelist = findPricelist(auctionId);
        Collections.sort(pricelist, new PriceComparator());

        double maxPrice = findMaxPrice(auctionId);

        // 1. 현재 상한가가 없는 경우 경매 시작가를 상한가로 반환
        if(pricelist == null || maxPrice == -1) {
            double startPrice = getStartPrice(auctionId);
            return new PricelistResponse(startPrice, pricelist);
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
    private double findMaxPrice(ObjectId auctionId) {
        Price maxPrice = auctionRepository.findById(auctionId).get().getPrice();
        if(maxPrice == null) return -1;
        return maxPrice.getAuctionPrice();
    }

    // 경매 시작가 조회
    private double getStartPrice(ObjectId auctionId) {
        Optional<Auction> auction = findAuctionByAuctionId(auctionId);
        Optional<Art> art = artRepository.findById(auction.get().getArtId());
        return art.get().getPrice();
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

    private void saveParticipantAuction(ObjectId priceId, ObjectId userId, double ratio, List<String> transactionHash) {
        ParticipantAuction participantAuction = ParticipantAuction.builder()
                .participantAuctionId(new ObjectId()).priceId(priceId).userId(userId)
                .ratio(ratio).rlp(transactionHash).build();
        participantAuctionRepository.save(participantAuction);
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

    // 낙찰 성공시 이전 경매 내역애서 상한가보다 낮은 경매 삭제 및 필드 value 추가
    private void successAuction(ObjectId auctionId, ObjectId priceId) {
        Optional<Price> newPrice = priceRepository.findById(priceId);
        if(!newPrice.isPresent()) return;

        Optional<Auction> auction = auctionRepository.findById(auctionId);
        if(auction.isPresent()) {
            Price maxPrice = auction.get().getPrice();
            if(maxPrice != null && maxPrice.getAuctionPrice() < newPrice.get().getAuctionPrice()) { // 상한가 갱신
                updateAuctionPrice(auctionId, newPrice.get());
                deleteInvalidAuctions(auctionId, newPrice.get().getAuctionPrice());
            }
            else if (maxPrice == null){ // 상한가 생성
                updateAuctionPrice(auctionId, newPrice.get());
            }
        }
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
    public void makePayment(LocalDate yesterday) {
        Optional<AuctionDate> auctionDate = auctionDateRepository.findByAuctionDate(yesterday);
        if(auctionDate.isPresent()){
            List<ObjectId> artIdList = auctionDate.get().getArtId();
            for(int i = 0; i < artIdList.size(); i++) {
                Optional<Auction> auction = auctionRepository.findByArtId(artIdList.get(i));
                if(!auction.isPresent()) continue;

                Price price = auction.get().getPrice();
                if(price == null) continue; // 낙찰이 되지 않은 경우 결제 진행하지 않음
                List<ParticipantAuction> participantAuctionList = participantAuctionRepository.findByPriceId(price.getPriceId());

                List<ObjectId> participantUserId = new ArrayList<>();   // 공동 투자자
                List<Double> participantRatio = new ArrayList<>();      // 공동 투자자별 지분
                List<String> rlp = new ArrayList<>();

                for(int j = 0; j < participantAuctionList.size(); j++) {
                    participantUserId.add(participantAuctionList.get(j).getUserId());
                    participantRatio.add(participantAuctionList.get(j).getRatio());
                    List<String> participantRlp = participantAuctionList.get(j).getRlp();
                    for(int k = 0; k < participantRlp.size(); k++) rlp.add(participantRlp.get(k));
                }

                // 결제 승인
                for(int j = 0; j < rlp.size(); j++) submitRlpTransaction(rlp.get(j));

                Optional<Art> art = artRepository.findById(auction.get().getArtId());
                if(art.isPresent()) {

                    // 그림 판매 내역 업데이트 (판매자 소유권 제거)
                    List<ObjectId> userIdList = art.get().getUserId();
                    for(int j= 0; j < userIdList.size(); j++) {
                        Optional<User> user = userService.findByUserId(userIdList.get(j));
                        if(!user.isPresent()) continue;
                        userService.removeArtWork(user.get(), art.get().getArtId());
                    }

                    // 낙찰 알림 메일 전송 및 그림 구매 내역 업데이트
                    for(int j = 0; j < participantUserId.size(); j++) {
                        Optional<User> user = userService.findByUserId(participantUserId.get(j));
                        if(!user.isPresent()) continue;
                        userService.buyArtwork(user.get(), art.get().getArtId());
                        userService.sendAuctionAlarm(user.get().getName(), art.get().getArtName(), art.get().getPaint(), price.getAuctionPrice(), user.get().getEmail());
                    }

                    // 판매 상태 변경 및 커뮤니티 생성
                    changeSoldState(art.get().getArtId(), price.getAuctionPrice(), participantUserId, participantRatio);
                    communityService.createCommunity(art.get().getArtId(), participantUserId, participantRatio);
                }
            }

        }
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
            System.out.println("SUBMIT TRANSACTION ERROR");
            System.out.println(e.getResponseBody());
        }
    }

    private void changeSoldState(ObjectId artId, double auctionPrice, List<ObjectId> userId, List<Double> ratio) { // 그림 상태 변경
        Optional<Art> art = artRepository.findById(artId);
        if(art.isPresent()) {
            art.get().setSold(true);
            art.get().setPrice(auctionPrice);
            art.get().setUserId(userId);
            art.get().setRatio(ratio);
            artRepository.save(art.get());
        }
    }

    public int countArtwork(LocalDate date) {
        Optional<AuctionDate> auctionDate = auctionDateRepository.findByAuctionDate(date);
        if(!auctionDate.isPresent()) return 0;
        else return auctionDate.get().getArtId().size();
    }

    public void setAuctionDate(ObjectId artId, LocalDate date) { // (작품 업로드)
        Optional<AuctionDate> auctionDate = auctionDateRepository.findByAuctionDate(date);
        if(auctionDate.isPresent()) updateAuctionDate(artId, auctionDate.get());
        else saveAuctionDate(date, artId);
    }

    private Object updateAuctionPrice(ObjectId auctionId, Price price) {
        Optional<Auction> auction = auctionRepository.findById(auctionId);
        if(auction.isPresent()) {
            auction.get().setPrice(price);
            return auctionRepository.save(auction.get());
        }
        return null;
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
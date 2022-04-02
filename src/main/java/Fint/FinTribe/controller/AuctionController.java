package Fint.FinTribe.controller;

import Fint.FinTribe.domain.auction.Auction;
import Fint.FinTribe.payload.request.*;
import Fint.FinTribe.payload.response.*;
import Fint.FinTribe.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import xyz.groundx.caver_ext_kas.rest_client.io.swagger.client.ApiException;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class AuctionController {
    private final AuctionService auctionService;

    // 1. 새로운 경매 가격 제안
    // 1-1. 거래 요청
    @PostMapping("/price")
    public ResponseEntity<?> newPriceRequest(@Valid @RequestBody NewPriceTransactionRequest newPriceTransactionRequest) {
        TransactionResponse transactionResponse = auctionService.newPrice(newPriceTransactionRequest);
        return new ResponseEntity<>(transactionResponse, HttpStatus.OK);
    }
    // 1-2. 거래 성사
    @PostMapping("/price-success")
    public ResponseEntity<?> newPriceSuccess(@Valid @RequestBody NewPriceRequest newPriceRequest) {
        NewPriceResponse newPriceResponse = auctionService.newPriceSuccess(newPriceRequest);
        return new ResponseEntity<>(newPriceResponse, HttpStatus.OK);
    }

    // 2. 기존 경매 참여
    // 2-1. 거래 요청
    @PostMapping("/participate")
    public ResponseEntity<?> participateAuctionRequest(@Valid @RequestBody ParticipateAuctionTransactionRequest participateAuctionTransactionRequest) {
        TransactionResponse transactionResponse = auctionService.participateAuction(participateAuctionTransactionRequest);
        return new ResponseEntity<>(transactionResponse, HttpStatus.OK);
    }
    // 2-2. 거래 성사
    @PostMapping("/participate-success")
    public ResponseEntity<?> participateAuctionSuccess(@Valid @RequestBody ParticipateAuctionRequest participateAuctionRequest) {
        ParticipateAuctionResponse participateAuctionResponse = auctionService.participateAuctionSuccess(participateAuctionRequest);
        return new ResponseEntity<>(participateAuctionResponse, HttpStatus.OK);
    }

    // 3. 현재 상한가 & 기존 경매 제안 리스트 받아오기
    @GetMapping("/pricelist")
    public ResponseEntity<?> getPricelist(@Valid @RequestBody PricelistRequest pricelistRequest) {
        PricelistResponse pricelistResponse = auctionService.getPricelist(pricelistRequest);
        return new ResponseEntity<>(pricelistResponse, HttpStatus.OK);
    }

    // 4. 현재 진행중인 경매 반환
    @GetMapping("/valid-auction")
    public ResponseEntity<?> getValidAuctions() {
        List<Auction> auctions = auctionService.getAuctions();
        return new ResponseEntity<>(new ValidAuctionResponse(auctions), HttpStatus.OK);
    }

    // 자정이되면 낙찰된 작품 결제 & 새로운 경매 작품 올리기
    @Scheduled(cron="0 0 0 * * *", zone="Asia/Seoul")
    public void startAuction() throws ApiException {
        // 결제
        auctionService.makePayment();
        // 다음 경매 시작
        LocalDate today = LocalDate.now();
        auctionService.deleteAuctions();
        auctionService.makeAuctions(today);
    }
}
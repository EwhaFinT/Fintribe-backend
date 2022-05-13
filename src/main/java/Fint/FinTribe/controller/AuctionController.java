package Fint.FinTribe.controller;

import Fint.FinTribe.payload.request.*;
import Fint.FinTribe.payload.response.*;
import Fint.FinTribe.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import xyz.groundx.caver_ext_kas.rest_client.io.swagger.client.ApiException;

import javax.validation.Valid;
import java.time.LocalDate;

@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class AuctionController {
    private final AuctionService auctionService;

    // 1. 새로운 경매 가격 제안
    @PostMapping("/price")
    public ResponseEntity<?> newPrice(@Valid @RequestBody NewPriceRequest newPriceRequest) {
        NewPriceResponse newPriceResponse = auctionService.newPrice(newPriceRequest);
        return new ResponseEntity<>(newPriceResponse, HttpStatus.OK);
    }

    // 2. 기존 경매 참여
    @PostMapping("/participate")
    public ResponseEntity<?> participateAuction(@Valid @RequestBody ParticipateAuctionRequest participateAuctionRequest) {
        ParticipateAuctionResponse participateAuctionResponse = auctionService.participateAuction(participateAuctionRequest);
        return new ResponseEntity<>(participateAuctionResponse, HttpStatus.OK);
    }

    // 3. 현재 상한가 & 기존 경매 제안 리스트 받아오기
    @GetMapping("/pricelist")
    public ResponseEntity<?> getPricelist(@Valid @RequestParam("artId") String artId) {
        PricelistResponse pricelistResponse = auctionService.getPricelist(new ObjectId(artId));
        return new ResponseEntity<>(pricelistResponse, HttpStatus.OK);
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
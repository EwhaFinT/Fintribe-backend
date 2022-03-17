package Fint.FinTribe.service;

import Fint.FinTribe.domain.art.ArtRepository;
import Fint.FinTribe.domain.auction.Auction;
import Fint.FinTribe.domain.auction.Price;
import Fint.FinTribe.payload.request.*;
import Fint.FinTribe.payload.response.PricelistResponse;
import org.assertj.core.api.Assertions;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
class AuctionServiceTest {

    @Autowired
    private ArtRepository artRepository;

    @Autowired
    private AuctionService auctionService;

    private List<Auction> auctions;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();
        auctionService.deleteAuctions();
        auctionService.makeAuctions(today);
        auctions = auctionService.getAuctions();
    }

    @AfterEach
    void tearDown() { /*artRepository.deleteAll(); auctionService.deleteAll();*/ }

//    테스트용 userId : 62325e0c9ff0d44d0ed82bf0 6232749829304e715460713e
//    테스트용 auctionId : 623329392862733ba84fc1d7
//    테스트용 priceId : 62327a05640e17746283fb9c

    @Test
    @DisplayName("새로운 경매 가격 제안 성공 테스트")
    void newPriceTransactionSuccess() {
        Auction auction = auctions.get(0);
        NewPriceTransactionRequest newPriceTransactionRequest = new NewPriceTransactionRequest(new ObjectId("62325e0c9ff0d44d0ed82bf0"), auction.getAuctionId(), 100, 1.0);
        Assertions.assertThat(auctionService.newPrice(newPriceTransactionRequest));
    }

    @Test
    @DisplayName("새로운 경매 가격 제안 실패 테스트")
    void newPriceTransactionFail() {
        Auction auction = auctions.get(0);
        NewPriceTransactionRequest newPriceTransactionRequest = new NewPriceTransactionRequest(new ObjectId("62325e0c9ff0d44d0ed82bf0"), auction.getAuctionId(), 25, 0.5);
        Assertions.assertThat(auctionService.newPrice(newPriceTransactionRequest));
    }

    @Test
    @DisplayName("새로운 경매 가격 성공 테스트")
    void newPrice() {
        Auction auction = auctions.get(0);
        NewPriceRequest newPriceRequest = new NewPriceRequest(new ObjectId("62325e0c9ff0d44d0ed82bf0"), auction.getAuctionId(), 100, 1.0, "rlp");
        Assertions.assertThat(auctionService.newPriceSuccess(newPriceRequest));
    }

    @Test
    @DisplayName("기존 경매 참여 성공 테스트 - 같은 회원")
    void participateAuctionSuccess1() {
        ObjectId userId = new ObjectId("62325e0c9ff0d44d0ed82bf0");
        ObjectId auctionId = new ObjectId("623329392862733ba84fc1d7");
        NewPriceRequest newPriceRequest = new NewPriceRequest(userId, auctionId, 150, 0.5, "rlp");
        ObjectId priceId = auctionService.newPriceSuccess(newPriceRequest).getPriceId();
        ParticipateAuctionRequest participateAuctionRequest = new ParticipateAuctionRequest(userId, priceId, auctionId, 0.3, "rlp");
        Assertions.assertThat(auctionService.participateAuctionSuccess(participateAuctionRequest).getPriceId()).isEqualTo(priceId);
    }

    @Test
    @DisplayName("기존 경매 참여 성공 테스트 - 서로 다른 회원")
    void participateAuctionSuccess2() {
        ObjectId userId1 = new ObjectId("62325e0c9ff0d44d0ed82bf0");
        ObjectId userId2 = new ObjectId("6232749829304e715460713e");
        ObjectId auctionId = new ObjectId("623329392862733ba84fc1d7");
        NewPriceRequest newPriceRequest = new NewPriceRequest(userId1, auctionId, 150, 0.5, "rlp");
        ObjectId priceId = auctionService.newPriceSuccess(newPriceRequest).getPriceId();
        ParticipateAuctionRequest participateAuctionRequest = new ParticipateAuctionRequest(userId2, priceId, auctionId, 0.5, "rlp");
        Assertions.assertThat(auctionService.participateAuctionSuccess(participateAuctionRequest).getPriceId()).isEqualTo(priceId);
    }


    @Test
    @DisplayName("기존 경매 참여 실패 테스트 - 잔여 지분 초과")
    void participateAuctionFail() {
        ObjectId userId = new ObjectId("62325e0c9ff0d44d0ed82bf0");
        ObjectId auctionId = new ObjectId("623329392862733ba84fc1d7");
        ObjectId priceId = new ObjectId("62327a05640e17746283fb9c");
        ParticipateAuctionTransactionRequest participateAuctionTransactionRequest = new ParticipateAuctionTransactionRequest(userId, priceId, auctionId, 0.5);
        Assertions.assertThat(auctionService.participateAuction(participateAuctionTransactionRequest).getTo()).isEqualTo(null);
    }

    @Test
    @DisplayName("기존 경매 제안 리스트 & 상한가 가져오기 - 상한가 존재")
    void getPricelist() {
        ObjectId auctionId = new ObjectId("623329392862733ba84fc1d7");
        PricelistRequest pricelistRequest = new PricelistRequest(auctionId);
        PricelistResponse pricelistResponse = auctionService.getPricelist(pricelistRequest);
        List<Price> pricelist = pricelistResponse.getPricelist();
        Assertions.assertThat(pricelistResponse.getPrice()).isNotEqualTo(pricelist.get(0).getAuctionPrice());
    }
}
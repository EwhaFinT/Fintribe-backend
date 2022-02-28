package Fint.FinTribe.service;

import Fint.FinTribe.domain.art.Art;
import Fint.FinTribe.domain.art.ArtRepository;
import Fint.FinTribe.domain.auction.Auction;
import Fint.FinTribe.domain.auction.AuctionRepository;
import Fint.FinTribe.domain.auction.Price;
import Fint.FinTribe.payload.request.NewPriceRequest;
import Fint.FinTribe.payload.request.ParticipateAuctionRequest;
import Fint.FinTribe.payload.request.PricelistRequest;
import Fint.FinTribe.payload.response.PricelistResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@SpringBootTest
class AuctionServiceTest {

    @Autowired
    private ArtRepository artRepository;

    @Autowired
    private AuctionService auctionService;

    @BeforeEach
    void setUp() {
        artRepository.save(
                Art.builder()
                        .artId("artId").artName("artName")
                        .paint("painter").detail("detail").price(50)
                        .nftAdd(null).paint(null).userId(null).build()
        );
        auctionService.createSampleAuction();
    }

    @AfterEach
    void tearDown() { artRepository.deleteAll(); auctionService.deleteAll(); }

    @Test
    @DisplayName("새로운 경매 가격 제안 성공 테스트")
    void newPriceSuccess() {
        NewPriceRequest newPriceRequest = new NewPriceRequest("userId-newPrice", "auctionId", 100, 0.5);
        Assertions.assertThat(auctionService.newPrice(newPriceRequest));
    }

    @Test
    @DisplayName("새로운 경매 가격 제안 성공 테스트")
    void newPriceFail() {
        NewPriceRequest newPriceRequest = new NewPriceRequest("userId-newPrice", "auctionId", 25, 0.5);
        Assertions.assertThat(auctionService.newPrice(newPriceRequest));
    }

    @Test
    @DisplayName("기존 경매 참여 성공 테스트 - 신규 회원")
    void participateAuctionSuccess1() {
        NewPriceRequest newPriceRequest = new NewPriceRequest("userId-participant1", "auctionId", 150, 0.5);
        Object priceId = auctionService.newPrice(newPriceRequest).getPriceId();
        ParticipateAuctionRequest participateAuctionRequest = new ParticipateAuctionRequest("userId-participant2", priceId, "auctionId", 0.3);
        Assertions.assertThat(auctionService.participateAuction(participateAuctionRequest).getPriceId()).isEqualTo(priceId);
    }

    @Test
    @DisplayName("기존 경매 참여 성공 테스트 - 기존 회원")
    void participateAuctionSuccess2() {
        NewPriceRequest newPriceRequest = new NewPriceRequest("userId-participant1", "auctionId", 150, 0.5);
        Object priceId = auctionService.newPrice(newPriceRequest).getPriceId();
        ParticipateAuctionRequest participateAuctionRequest = new ParticipateAuctionRequest("userId-participant1", priceId, "auctionId", 0.3);
        Assertions.assertThat(auctionService.participateAuction(participateAuctionRequest).getPriceId()).isEqualTo(priceId);
    }

    @Test
    @DisplayName("기존 경매 참여 실패 테스트 - 잔여 지분 초과")
    void participateAuctionFail() {
        NewPriceRequest newPriceRequest = new NewPriceRequest("userId-participantFail1", "auctionId", 200, 0.6);
        Object priceId = auctionService.newPrice(newPriceRequest);
        ParticipateAuctionRequest participateAuctionRequest = new ParticipateAuctionRequest("userId-participantFail2", priceId, "auctionId", 0.5);
        Assertions.assertThat(auctionService.participateAuction(participateAuctionRequest).getPriceId()).isEqualTo(0);
    }

    @Test
    @DisplayName("기존 경매 제안 리스트 & 상한가 가져오기 - 상한가 존재X")
    void getPricelist1() {
        auctionService.newPrice(new NewPriceRequest("userId1", "auctionId", 100, 0.5));
        auctionService.newPrice(new NewPriceRequest("userId2", "auctionId", 150, 0.5));
        auctionService.newPrice(new NewPriceRequest("userId3", "auctionId", 200, 0.4));
        PricelistRequest pricelistRequest = new PricelistRequest("auctionId");
        PricelistResponse pricelistResponse = auctionService.getPricelist(pricelistRequest);
        List<Price> pricelist = pricelistResponse.getPricelist();
        Assertions.assertThat(pricelistResponse.getPrice()).isNotEqualTo(pricelist.get(0).getAuctionPrice());
    }

    @Test
    @DisplayName("기존 경매 제안 리스트 & 상한가 가져오기 - 상한가 존재")
    void getPricelist2() {
        auctionService.newPrice(new NewPriceRequest("userId1", "auctionId", 100, 0.5));
        auctionService.newPrice(new NewPriceRequest("userId2", "auctionId", 150, 1.0));
        auctionService.newPrice(new NewPriceRequest("userId3", "auctionId", 200, 0.4));
        PricelistRequest pricelistRequest = new PricelistRequest("auctionId");
        PricelistResponse pricelistResponse = auctionService.getPricelist(pricelistRequest);
        List<Price> pricelist = pricelistResponse.getPricelist();
        Assertions.assertThat(pricelistResponse.getPrice()).isNotEqualTo(pricelist.get(0).getAuctionPrice());
    }
}
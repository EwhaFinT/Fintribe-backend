package Fint.FinTribe.service;

import Fint.FinTribe.payload.request.UploadRequest;
import Fint.FinTribe.payload.response.UploadResponse;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
class ArtServiceTest {
    @Autowired
    private ArtService artService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("작품 등록")
    void upload() {
        UploadRequest uploadRequest = new UploadRequest(
                new ObjectId("62325e0c9ff0d44d0ed82bf0"),
                "C://sample_artwork.jpg",
                "artName", "painter", 10,
                LocalDate.now(), "detail");
        UploadResponse uploadResponse = artService.upload(uploadRequest);
    }
}
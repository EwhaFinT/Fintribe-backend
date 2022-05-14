package Fint.FinTribe.payload.request;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UploadRequest {
    @NonNull
    private String userId;
    @NonNull
    private String artName;
    @NonNull
    private String painter;
    @NonNull
    private double price;
    @NonNull
    private LocalDateTime auctionDate;
    @NonNull
    private String detail;
    @NonNull
    private String paint;
}
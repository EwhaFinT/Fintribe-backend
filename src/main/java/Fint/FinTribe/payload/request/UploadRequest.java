package Fint.FinTribe.payload.request;

import lombok.*;
import org.bson.types.ObjectId;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class UploadRequest {
    @NonNull
    private ObjectId userId;
    @NonNull
    private String filePath;
    @NonNull
    private String artName;
    @NonNull
    private String painter;
    @NonNull
    private double price;
    @NonNull
    private LocalDate auctionDate;
    @NonNull
    private String detail;
}
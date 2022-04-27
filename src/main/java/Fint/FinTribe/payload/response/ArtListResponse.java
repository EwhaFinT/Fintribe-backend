package Fint.FinTribe.payload.response;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
public class ArtListResponse {
    private List<String> artId;
    private List<String> paint; // (== url)
}
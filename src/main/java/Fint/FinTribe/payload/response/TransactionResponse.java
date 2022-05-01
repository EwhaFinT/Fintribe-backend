package Fint.FinTribe.payload.response;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
public class TransactionResponse {
    private List<String> to;
    private List<Double> value;
}
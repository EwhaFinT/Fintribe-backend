package Fint.FinTribe.payload.response;

import lombok.*;

@Getter
@AllArgsConstructor
public class TransactionResponse {
    private double gas;
    private String to;
    private double value;
}
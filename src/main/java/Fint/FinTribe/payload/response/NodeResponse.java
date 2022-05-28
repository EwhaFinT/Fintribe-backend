package Fint.FinTribe.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class NodeResponse {
    @NonNull
    private String transactionHash;
}

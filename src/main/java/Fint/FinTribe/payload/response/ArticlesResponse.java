package Fint.FinTribe.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ArticlesResponse {
    private List<ArticlesResTmp> articles;
}

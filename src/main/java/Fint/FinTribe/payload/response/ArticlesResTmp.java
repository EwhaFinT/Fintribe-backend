package Fint.FinTribe.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ArticlesResTmp {
    private String articleId;
    private String title;
    private LocalDateTime createdAt;
}

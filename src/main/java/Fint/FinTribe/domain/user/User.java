package Fint.FinTribe.domain.user;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Builder
@Document(collection = "user")
public class User {
    @Id
    private Object userId;
    private List<Object> artId;
    private String identity;
    private String pw;
    private String wallet;
    private String name;
    private String phone;
    private String email;
}

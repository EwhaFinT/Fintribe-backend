package Fint.FinTribe.domain.user;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@Document(collection = "user")
public class User {
    @Id
    private ObjectId userId;
    private List<ObjectId> artId;
    private String identity;
    private String pw;
    private String wallet;
    private String name;
    private String phone;
    private String email;
}

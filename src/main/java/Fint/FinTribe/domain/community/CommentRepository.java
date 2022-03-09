package Fint.FinTribe.domain.community;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, Integer> {
    @Query("{'articleId': ?0}")
    List<Comment> findByArticleId(ObjectId articleId);
}

package Fint.FinTribe.domain.community;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends MongoRepository<Comment, Integer> {
    @Query("{'articleId': ?0}")
    List<Comment> findByArticleId(ObjectId articleId);

    @Query("{$and:[{'_id': ?0}, {'articleId': ?0}]")
    Optional<Comment> findByIdAndArticleId(Integer commentId, ObjectId articleId);
}

package Fint.FinTribe.domain.community;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReCommentRepository extends MongoRepository<ReComment, Integer> {
    @Query("{$and:[{'articleId': ?0}, {'tagCommentId': ?1}]}")
    List<ReComment> findByArticleIdAndTagCommentId(ObjectId articleId, Integer commentId);

    @Query("{$and:[{'_id': ?0}, {'tagCommentId': ?1}, {'articleId': ?2}]}")
    Optional<ReComment> findByIdAndTagCommentIdAndArticleId(Integer commentId, Integer tagCommentId, ObjectId articleId);

    @Query("{'articleId': ?0}")
    List<ReComment> findByArticleId(ObjectId articleId);
}

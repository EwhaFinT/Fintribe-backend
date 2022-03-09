package Fint.FinTribe.domain.community;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ReCommentRepository extends MongoRepository<Comment, Integer> {
    @Query("{'tagCommentId': ?0}")
    List<ReComment> findByTagCommentId(Integer commentId);
}

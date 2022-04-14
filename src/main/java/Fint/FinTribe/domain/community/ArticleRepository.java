package Fint.FinTribe.domain.community;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends MongoRepository<Article, ObjectId> {
    @Query("{$and:[{'communityId': ?0}, {'isDeleted': ?1}]}")
    List<Article> findByCommunityIdAndIsDeleted(ObjectId communityId,Boolean isDeleted);

    @Query("{$and:[{'articleId': ?0}, {'isDeleted': ?1}]}")
    Optional<Article> findByArticleIdAndIsDeleted(ObjectId articleId, Boolean isDeleted);
}

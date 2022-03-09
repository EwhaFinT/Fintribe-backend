package Fint.FinTribe.domain.community;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ArticleRepository extends MongoRepository<Article, ObjectId> {
    @Query("{$and:[{'communityId': ?0}, {'isDeleted': ?0}]")
    List<Article> findByCommunityIdAndIsDeleted(ObjectId communityId,Boolean isDeleted);
}

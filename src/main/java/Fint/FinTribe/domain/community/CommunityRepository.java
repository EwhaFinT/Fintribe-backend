package Fint.FinTribe.domain.community;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityRepository extends MongoRepository<Community, ObjectId> {
    @Query("{$and:[{'artId': ?0}, {'isDeleted': ?1}]}")
    Optional<Community> findByArtIdAndIsDeleted(ObjectId objectId, Boolean isDeleted);
}

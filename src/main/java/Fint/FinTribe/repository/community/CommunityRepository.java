package Fint.FinTribe.repository.community;

import Fint.FinTribe.domain.community.Community;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityRepository extends MongoRepository<Community, ObjectId> {
    Optional<Community> findByArtId(ObjectId objectId);
}

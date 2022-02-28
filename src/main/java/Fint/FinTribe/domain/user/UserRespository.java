package Fint.FinTribe.domain.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRespository extends MongoRepository<User, Object> {
    @Query("{'identity': ?0}")
    Optional<User> findByIdentity(String identity);
}
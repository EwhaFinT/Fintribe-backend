package Fint.FinTribe.domain.user;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRespository extends MongoRepository<User, ObjectId> {
    @Query("{'identity': ?0}")
    Optional<User> findByIdentity(String identity);

    @Query("{'name': ?0, 'phone': ?1}")
    Optional<User> findByNameAndPhone(String name, String phone);

    @Query("{'identity': ?0, 'email': ?1}")
    Optional<User> findByIdentityAndEmail(String identity, String email);
}
package Fint.FinTribe.domain.resaleDate;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ResaleDateRepository extends MongoRepository<ResaleDate, ObjectId> {
    @Query("{'resaleDate': ?0}")
    Optional<ResaleDate> findByResaleDate(LocalDate resaleDate);
}
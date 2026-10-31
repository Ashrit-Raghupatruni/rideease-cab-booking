package com.rideease.repository;

import com.rideease.model.SafetyContact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SafetyContactRepository extends MongoRepository<SafetyContact, String> {
    List<SafetyContact> findByUserId(String userId);
}

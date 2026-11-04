package com.rideease.repository;

import com.rideease.model.SafetyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SafetyContactRepository extends JpaRepository<SafetyContact, String> {
    List<SafetyContact> findByUserId(String userId);
}

package com.example.restlab.repository;

import com.example.restlab.model.entity.ThirdPartyCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThirdPartyCredentialRepository extends JpaRepository<ThirdPartyCredential, Long> {

    List<ThirdPartyCredential> findByUserId(Long userId);

    Optional<ThirdPartyCredential> findByServiceNameAndUserId(String serviceName, Long userId);
}

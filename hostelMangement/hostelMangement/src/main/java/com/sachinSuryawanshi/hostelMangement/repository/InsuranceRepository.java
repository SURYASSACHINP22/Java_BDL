package com.sachinSuryawanshi.hostelMangement.repository;

import com.sachinSuryawanshi.hostelMangement.entity.Insurance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InsuranceRepository extends JpaRepository<Insurance, Long> {
    Optional<Insurance> findByPolicyNumber(String policyNumber);

    boolean existsByPolicyNumber(String policyNumber);

    List<Insurance> findByValidUntilBefore(LocalDate date);
}

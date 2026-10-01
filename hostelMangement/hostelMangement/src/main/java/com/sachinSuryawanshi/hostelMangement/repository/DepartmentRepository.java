package com.sachinSuryawanshi.hostelMangement.repository;

import com.sachinSuryawanshi.hostelMangement.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);

    boolean existsByName(String name);

    Optional<Department> findByHeadDoctorId(Long doctorId);

    @Query("select d from Department d join d.doctors doc where doc.id = :doctorId")
    List<Department> findAllByDoctorId(@Param("doctorId") Long doctorId);
}

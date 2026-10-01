package com.sachinSuryawanshi.hostelMangement.repository;

import com.sachinSuryawanshi.hostelMangement.DTO.BloodGroupCountResponseEntity;
import com.sachinSuryawanshi.hostelMangement.entity.Patient;
import com.sachinSuryawanshi.hostelMangement.entity.type.BloodGroupType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Patient findByName(String name);

    boolean existsByEmail(String email);

    Patient findByBirthDate(LocalDate birthDate);

    Patient findByBirthDateOrEmail(LocalDate birthDate, String email);

    @Query("select p from Patient p where p.bloodGroup = ?1")
    List<Patient> findByBloodgroup(@Param("bloodGroup") BloodGroupType bloodGroup);

    @Query("select p from Patient p where p.birthDate > :birthDate")
    List<Patient> findByBornAfterDate(@Param("birthDate") LocalDate birthDate);

    @Query("select p.bloodGroup,count(p) from Patient p group by p.bloodGroup")
    List<Object[]> findByBloodgroupType();

    @Query(value = "select * from patient",nativeQuery = true)
    List<Patient> findAllPatients();

    @Transactional
    @Modifying
    @Query("update Patient p set p.name = :name where p.id = :id")
    int updateNameWithId(@Param("name")String name, @Param("id")Long id);


    //projection
    @Query("select new com.sachinSuryawanshi.hostelMangement.DTO.BloodGroupCountResponseEntity(p.bloodGroup," +
            "count(p)) from Patient p group by p.bloodGroup")
    List<BloodGroupCountResponseEntity> findByBloodgroup();

    @Query(value = "select * from patient", countQuery = "select count(*) from patient", nativeQuery = true)
    Page<Patient> findAllPatients(Pageable pageable);

}

package com.sachinSuryawanshi.hostelMangement.DTO;

import com.sachinSuryawanshi.hostelMangement.entity.Patient;
import com.sachinSuryawanshi.hostelMangement.entity.type.BloodGroupType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientResponseDto(
        Long id,
        String name,
        String gender,
        LocalDate birthDate,
        String email,
        BloodGroupType bloodGroup,
        LocalDateTime createdAt,
        InsuranceResponseDto insurance
) {
    public static PatientResponseDto from(Patient patient) {
        return new PatientResponseDto(
                patient.getId(),
                patient.getName(),
                patient.getGender(),
                patient.getBirthDate(),
                patient.getEmail(),
                patient.getBloodGroup(),
                patient.getCreatedAt(),
                patient.getInsurance() == null ? null : InsuranceResponseDto.from(patient.getInsurance())
        );
    }
}

package com.sachinSuryawanshi.hostelMangement.DTO;

import com.sachinSuryawanshi.hostelMangement.entity.type.BloodGroupType;

import java.time.LocalDate;

public record PatientRequestDto(
        String name,
        String gender,
        LocalDate birthDate,
        String email,
        BloodGroupType bloodGroup
) {
}

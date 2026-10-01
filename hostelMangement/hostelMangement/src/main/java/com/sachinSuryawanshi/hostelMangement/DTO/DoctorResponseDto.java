package com.sachinSuryawanshi.hostelMangement.DTO;

import com.sachinSuryawanshi.hostelMangement.entity.Doctor;

import java.time.LocalDateTime;

public record DoctorResponseDto(
        Long id,
        String name,
        String specialization,
        String email,
        LocalDateTime createdAt
) {
    public static DoctorResponseDto from(Doctor doctor) {
        return new DoctorResponseDto(
                doctor.getId(),
                doctor.getName(),
                doctor.getSpecialization(),
                doctor.getEmail(),
                doctor.getCreatedAt()
        );
    }
}

package com.sachinSuryawanshi.hostelMangement.DTO;

import com.sachinSuryawanshi.hostelMangement.entity.Department;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record DepartmentResponseDto(
        Long id,
        String name,
        LocalDateTime createdAt,
        DoctorResponseDto headDoctor,
        List<DoctorResponseDto> doctors
) {
    public static DepartmentResponseDto from(Department department) {
        return new DepartmentResponseDto(
                department.getId(),
                department.getName(),
                department.getCreatedAt(),
                DoctorResponseDto.from(department.getHeadDoctor()),
                department.getDoctors().stream()
                        .map(DoctorResponseDto::from)
                        .sorted(Comparator.comparing(DoctorResponseDto::id))
                        .toList()
        );
    }
}

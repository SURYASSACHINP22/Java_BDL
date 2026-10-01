package com.sachinSuryawanshi.hostelMangement.DTO;

import com.sachinSuryawanshi.hostelMangement.entity.Insurance;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record InsuranceResponseDto(
        Long id,
        String policyNumber,
        String provider,
        LocalDate validUntil,
        LocalDateTime createdAt
) {
    public static InsuranceResponseDto from(Insurance insurance) {
        return new InsuranceResponseDto(
                insurance.getId(),
                insurance.getPolicyNumber(),
                insurance.getProvider(),
                insurance.getValidUntil(),
                insurance.getCreatedAt()
        );
    }
}

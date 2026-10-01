package com.sachinSuryawanshi.hostelMangement.DTO;

import java.time.LocalDate;

public record InsuranceRequestDto(
        String policyNumber,
        String provider,
        LocalDate validUntil
) {
}

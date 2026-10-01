package com.sachinSuryawanshi.hostelMangement.DTO;

import java.time.LocalDateTime;

public record AppointmentRequestDto(
        LocalDateTime appointmentTime,
        String reason,
        Long doctorId,
        Long patientId
) {
}

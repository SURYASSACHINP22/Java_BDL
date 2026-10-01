package com.sachinSuryawanshi.hostelMangement.DTO;

import com.sachinSuryawanshi.hostelMangement.entity.Appointment;
import com.sachinSuryawanshi.hostelMangement.entity.type.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponseDto(
        Long id,
        LocalDateTime appointmentTime,
        String reason,
        AppointmentStatus status,
        Long doctorId,
        String doctorName,
        Long patientId,
        String patientName
) {
    public static AppointmentResponseDto from(Appointment appointment) {
        return new AppointmentResponseDto(
                appointment.getId(),
                appointment.getAppointmentTime(),
                appointment.getReason(),
                appointment.getStatus(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getName(),
                appointment.getPatient().getId(),
                appointment.getPatient().getName()
        );
    }
}

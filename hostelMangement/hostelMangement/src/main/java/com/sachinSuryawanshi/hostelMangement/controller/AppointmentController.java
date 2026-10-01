package com.sachinSuryawanshi.hostelMangement.controller;

import com.sachinSuryawanshi.hostelMangement.DTO.AppointmentRequestDto;
import com.sachinSuryawanshi.hostelMangement.DTO.AppointmentResponseDto;
import com.sachinSuryawanshi.hostelMangement.entity.type.AppointmentStatus;
import com.sachinSuryawanshi.hostelMangement.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    public List<AppointmentResponseDto> getAllAppointments(@RequestParam(required = false) AppointmentStatus status) {
        return appointmentService.getAllAppointments(status).stream()
                .map(AppointmentResponseDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public AppointmentResponseDto getAppointment(@PathVariable Long id) {
        return AppointmentResponseDto.from(appointmentService.getAppointment(id));
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDto> createAppointment(@RequestBody AppointmentRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AppointmentResponseDto.from(appointmentService.createNewAppointment(request)));
    }

    @PutMapping("/{id}/doctor/{doctorId}")
    public AppointmentResponseDto reassignDoctor(@PathVariable Long id, @PathVariable Long doctorId) {
        return AppointmentResponseDto.from(appointmentService.reAssignAppointmentToAnotherDoctor(id, doctorId));
    }

    @PatchMapping("/{id}/cancel")
    public AppointmentResponseDto cancel(@PathVariable Long id) {
        return AppointmentResponseDto.from(appointmentService.cancelAppointment(id));
    }

    @PatchMapping("/{id}/complete")
    public AppointmentResponseDto complete(@PathVariable Long id) {
        return AppointmentResponseDto.from(appointmentService.completeAppointment(id));
    }
}

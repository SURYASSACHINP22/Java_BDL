package com.sachinSuryawanshi.hostelMangement.controller;

import com.sachinSuryawanshi.hostelMangement.DTO.AppointmentResponseDto;
import com.sachinSuryawanshi.hostelMangement.DTO.DepartmentResponseDto;
import com.sachinSuryawanshi.hostelMangement.DTO.DoctorRequestDto;
import com.sachinSuryawanshi.hostelMangement.DTO.DoctorResponseDto;
import com.sachinSuryawanshi.hostelMangement.service.AppointmentService;
import com.sachinSuryawanshi.hostelMangement.service.DepartmentService;
import com.sachinSuryawanshi.hostelMangement.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final DepartmentService departmentService;
    private final AppointmentService appointmentService;

    @GetMapping
    public List<DoctorResponseDto> getAllDoctors(@RequestParam(required = false) String specialization) {
        return doctorService.getAllDoctors(specialization).stream()
                .map(DoctorResponseDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public DoctorResponseDto getDoctor(@PathVariable Long id) {
        return DoctorResponseDto.from(doctorService.getDoctor(id));
    }

    @PostMapping
    public ResponseEntity<DoctorResponseDto> createDoctor(@RequestBody DoctorRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DoctorResponseDto.from(doctorService.createDoctor(request)));
    }

    @PutMapping("/{id}")
    public DoctorResponseDto updateDoctor(@PathVariable Long id, @RequestBody DoctorRequestDto request) {
        return DoctorResponseDto.from(doctorService.updateDoctor(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/appointments")
    public List<AppointmentResponseDto> getAppointmentsOfDoctor(@PathVariable Long id) {
        return appointmentService.getAppointmentsOfDoctor(id).stream()
                .map(AppointmentResponseDto::from)
                .toList();
    }

    @GetMapping("/{id}/departments")
    public List<DepartmentResponseDto> getDepartmentsOfDoctor(@PathVariable Long id) {
        return departmentService.getDepartmentsOfDoctor(id).stream()
                .map(DepartmentResponseDto::from)
                .toList();
    }
}

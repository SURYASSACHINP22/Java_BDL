package com.sachinSuryawanshi.hostelMangement.controller;

import com.sachinSuryawanshi.hostelMangement.DTO.AppointmentResponseDto;
import com.sachinSuryawanshi.hostelMangement.DTO.BloodGroupCountResponseEntity;
import com.sachinSuryawanshi.hostelMangement.DTO.InsuranceRequestDto;
import com.sachinSuryawanshi.hostelMangement.DTO.InsuranceResponseDto;
import com.sachinSuryawanshi.hostelMangement.DTO.PatientRequestDto;
import com.sachinSuryawanshi.hostelMangement.DTO.PatientResponseDto;
import com.sachinSuryawanshi.hostelMangement.service.AppointmentService;
import com.sachinSuryawanshi.hostelMangement.service.InsuranceService;
import com.sachinSuryawanshi.hostelMangement.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final InsuranceService insuranceService;
    private final AppointmentService appointmentService;

    @GetMapping
    public PagedModel<PatientResponseDto> getAllPatients(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return new PagedModel<>(patientService.getAllPatients(PageRequest.of(page, size, Sort.by("id")))
                .map(PatientResponseDto::from));
    }

    @GetMapping("/{id}")
    public PatientResponseDto getPatient(@PathVariable Long id) {
        return PatientResponseDto.from(patientService.getPatient(id));
    }

    @PostMapping
    public ResponseEntity<PatientResponseDto> createPatient(@RequestBody PatientRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PatientResponseDto.from(patientService.createPatient(request)));
    }

    @PutMapping("/{id}")
    public PatientResponseDto updatePatient(@PathVariable Long id, @RequestBody PatientRequestDto request) {
        return PatientResponseDto.from(patientService.updatePatient(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/blood-groups")
    public List<BloodGroupCountResponseEntity> getBloodGroupStats() {
        return patientService.getBloodGroupStats();
    }

    @GetMapping("/{id}/appointments")
    public List<AppointmentResponseDto> getAppointmentsOfPatient(@PathVariable Long id) {
        return appointmentService.getAppointmentsOfPatient(id).stream()
                .map(AppointmentResponseDto::from)
                .toList();
    }

    @GetMapping("/{id}/insurance")
    public InsuranceResponseDto getInsurance(@PathVariable Long id) {
        return InsuranceResponseDto.from(insuranceService.getInsuranceOfPatient(id));
    }

    @PutMapping("/{id}/insurance")
    public PatientResponseDto assignInsurance(@PathVariable Long id, @RequestBody InsuranceRequestDto request) {
        return PatientResponseDto.from(insuranceService.assignInsuranceToPatient(id, request));
    }

    @DeleteMapping("/{id}/insurance")
    public PatientResponseDto removeInsurance(@PathVariable Long id) {
        return PatientResponseDto.from(insuranceService.removeInsuranceFromPatient(id));
    }
}

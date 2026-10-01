package com.sachinSuryawanshi.hostelMangement.service;


import com.sachinSuryawanshi.hostelMangement.DTO.BloodGroupCountResponseEntity;
import com.sachinSuryawanshi.hostelMangement.DTO.PatientRequestDto;
import com.sachinSuryawanshi.hostelMangement.entity.Patient;
import com.sachinSuryawanshi.hostelMangement.exception.BusinessRuleException;
import com.sachinSuryawanshi.hostelMangement.exception.ResourceNotFoundException;
import com.sachinSuryawanshi.hostelMangement.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;


    @Transactional
    public Patient getPatientById(Long id){
        Patient patient1 = patientRepository.findById(id).orElseThrow();

        Patient patient2 = patientRepository.findById(id).orElseThrow();

        return patient1;

    }

    @Transactional(readOnly = true)
    public Patient getPatient(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
    }

    @Transactional(readOnly = true)
    public Page<Patient> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable);
    }

    @Transactional
    public Patient createPatient(PatientRequestDto request) {
        validate(request);
        if (patientRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("A patient with email " + request.email() + " already exists");
        }

        Patient patient = new Patient();
        copy(request, patient);
        return patientRepository.save(patient);
    }

    @Transactional
    public Patient updatePatient(Long id, PatientRequestDto request) {
        validate(request);
        Patient patient = getPatient(id);
        if (!patient.getEmail().equalsIgnoreCase(request.email()) && patientRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("A patient with email " + request.email() + " already exists");
        }

        copy(request, patient);
        return patient; // dirty checking flushes the changes
    }

    // appointments and insurance are removed through cascade / orphanRemoval
    @Transactional
    public void deletePatient(Long id) {
        patientRepository.delete(getPatient(id));
    }

    @Transactional(readOnly = true)
    public List<BloodGroupCountResponseEntity> getBloodGroupStats() {
        return patientRepository.findByBloodgroup();
    }

    private void validate(PatientRequestDto request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Patient name is required");
        }
        if (request.name().length() > 20) {
            throw new IllegalArgumentException("Patient name must be at most 20 characters");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Patient email is required");
        }
    }

    private void copy(PatientRequestDto request, Patient patient) {
        patient.setName(request.name());
        patient.setGender(request.gender());
        patient.setBirthDate(request.birthDate());
        patient.setEmail(request.email());
        patient.setBloodGroup(request.bloodGroup());
    }
}

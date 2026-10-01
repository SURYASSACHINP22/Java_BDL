package com.sachinSuryawanshi.hostelMangement.service;

import com.sachinSuryawanshi.hostelMangement.DTO.InsuranceRequestDto;
import com.sachinSuryawanshi.hostelMangement.entity.Insurance;
import com.sachinSuryawanshi.hostelMangement.entity.Patient;
import com.sachinSuryawanshi.hostelMangement.exception.BusinessRuleException;
import com.sachinSuryawanshi.hostelMangement.exception.ResourceNotFoundException;
import com.sachinSuryawanshi.hostelMangement.repository.InsuranceRepository;
import com.sachinSuryawanshi.hostelMangement.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class InsuranceService {

    private final InsuranceRepository insuranceRepository;
    private final PatientRepository patientRepository;

    // creates the insurance, or replaces the patient's existing one
    @Transactional
    public Patient assignInsuranceToPatient(Long patientId, InsuranceRequestDto request) {
        validate(request);
        Patient patient = findPatient(patientId);

        Insurance current = patient.getInsurance();
        if (current != null && current.getPolicyNumber().equals(request.policyNumber())) {
            // same policy -> just renew / update it
            current.setProvider(request.provider());
            current.setValidUntil(request.validUntil());
            return patient;
        }

        if (insuranceRepository.existsByPolicyNumber(request.policyNumber())) {
            throw new BusinessRuleException("Policy number " + request.policyNumber() + " is already in use");
        }

        Insurance insurance = new Insurance();
        insurance.setPolicyNumber(request.policyNumber());
        insurance.setProvider(request.provider());
        insurance.setValidUntil(request.validUntil());
        insurance.setPatient(patient);

        patient.setInsurance(insurance); // cascade persists it, orphanRemoval deletes the old one
        return patient;
    }

    @Transactional
    public Patient removeInsuranceFromPatient(Long patientId) {
        Patient patient = findPatient(patientId);
        if (patient.getInsurance() == null) {
            throw new BusinessRuleException("Patient " + patientId + " has no insurance");
        }

        patient.getInsurance().setPatient(null);
        patient.setInsurance(null); // orphanRemoval deletes the insurance row
        return patient;
    }

    @Transactional(readOnly = true)
    public Insurance getInsuranceOfPatient(Long patientId) {
        Patient patient = findPatient(patientId);
        if (patient.getInsurance() == null) {
            throw new BusinessRuleException("Patient " + patientId + " has no insurance");
        }
        return patient.getInsurance();
    }

    private Patient findPatient(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));
    }

    private void validate(InsuranceRequestDto request) {
        if (request.policyNumber() == null || request.policyNumber().isBlank()) {
            throw new IllegalArgumentException("Policy number is required");
        }
        if (request.provider() == null || request.provider().isBlank()) {
            throw new IllegalArgumentException("Insurance provider is required");
        }
        if (request.validUntil() == null || request.validUntil().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Insurance validUntil must be today or a future date");
        }
    }
}

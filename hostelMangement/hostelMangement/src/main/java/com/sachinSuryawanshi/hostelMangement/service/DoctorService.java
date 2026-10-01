package com.sachinSuryawanshi.hostelMangement.service;

import com.sachinSuryawanshi.hostelMangement.DTO.DoctorRequestDto;
import com.sachinSuryawanshi.hostelMangement.entity.Department;
import com.sachinSuryawanshi.hostelMangement.entity.Doctor;
import com.sachinSuryawanshi.hostelMangement.exception.BusinessRuleException;
import com.sachinSuryawanshi.hostelMangement.exception.ResourceNotFoundException;
import com.sachinSuryawanshi.hostelMangement.repository.AppointmentRepository;
import com.sachinSuryawanshi.hostelMangement.repository.DepartmentRepository;
import com.sachinSuryawanshi.hostelMangement.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public Doctor getDoctor(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
    }

    @Transactional(readOnly = true)
    public List<Doctor> getAllDoctors(String specialization) {
        if (specialization == null || specialization.isBlank()) {
            return doctorRepository.findAll();
        }
        return doctorRepository.findBySpecializationIgnoreCase(specialization);
    }

    @Transactional
    public Doctor createDoctor(DoctorRequestDto request) {
        validate(request);
        if (doctorRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("A doctor with email " + request.email() + " already exists");
        }

        Doctor doctor = new Doctor();
        copy(request, doctor);
        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor updateDoctor(Long id, DoctorRequestDto request) {
        validate(request);
        Doctor doctor = getDoctor(id);
        if (!doctor.getEmail().equalsIgnoreCase(request.email()) && doctorRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("A doctor with email " + request.email() + " already exists");
        }

        copy(request, doctor);
        return doctor;
    }

    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctor(id);

        departmentRepository.findByHeadDoctorId(id).ifPresent(d -> {
            throw new BusinessRuleException("Doctor " + id + " is head of department '" + d.getName()
                    + "'. Assign a new head before deleting");
        });
        if (appointmentRepository.existsByDoctorId(id)) {
            throw new BusinessRuleException("Doctor " + id + " has appointments. Reassign them before deleting");
        }

        // clean up the doctor_department rows (Department owns that relationship)
        for (Department department : departmentRepository.findAllByDoctorId(id)) {
            department.getDoctors().remove(doctor);
        }
        doctorRepository.delete(doctor);
    }

    private void validate(DoctorRequestDto request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Doctor name is required");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Doctor email is required");
        }
    }

    private void copy(DoctorRequestDto request, Doctor doctor) {
        doctor.setName(request.name());
        doctor.setSpecialization(request.specialization());
        doctor.setEmail(request.email());
    }
}

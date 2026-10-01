package com.sachinSuryawanshi.hostelMangement.service;

import com.sachinSuryawanshi.hostelMangement.DTO.DepartmentRequestDto;
import com.sachinSuryawanshi.hostelMangement.entity.Department;
import com.sachinSuryawanshi.hostelMangement.entity.Doctor;
import com.sachinSuryawanshi.hostelMangement.exception.BusinessRuleException;
import com.sachinSuryawanshi.hostelMangement.exception.ResourceNotFoundException;
import com.sachinSuryawanshi.hostelMangement.repository.DepartmentRepository;
import com.sachinSuryawanshi.hostelMangement.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }

    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Department> getDepartmentsOfDoctor(Long doctorId) {
        findDoctor(doctorId);
        return departmentRepository.findAllByDoctorId(doctorId);
    }

    // the head doctor is automatically a member of the department
    @Transactional
    public Department createDepartment(DepartmentRequestDto request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Department name is required");
        }
        if (request.headDoctorId() == null) {
            throw new IllegalArgumentException("headDoctorId is required");
        }
        if (departmentRepository.existsByName(request.name())) {
            throw new BusinessRuleException("Department '" + request.name() + "' already exists");
        }

        Doctor head = findDoctor(request.headDoctorId());
        ensureNotHeadElsewhere(head, null);

        Department department = new Department();
        department.setName(request.name());
        department.setHeadDoctor(head);
        department.getDoctors().add(head);
        head.getDepartments().add(department);
        return departmentRepository.save(department);
    }

    @Transactional
    public Department addDoctorToDepartment(Long departmentId, Long doctorId) {
        Department department = getDepartment(departmentId);
        Doctor doctor = findDoctor(doctorId);

        if (!department.getDoctors().add(doctor)) {
            throw new BusinessRuleException("Doctor " + doctorId + " is already assigned to '" + department.getName() + "'");
        }
        doctor.getDepartments().add(department);
        return department;
    }

    @Transactional
    public Department removeDoctorFromDepartment(Long departmentId, Long doctorId) {
        Department department = getDepartment(departmentId);
        Doctor doctor = findDoctor(doctorId);

        if (department.getHeadDoctor().getId().equals(doctorId)) {
            throw new BusinessRuleException("Cannot remove the head doctor. Assign a new head first");
        }
        if (!department.getDoctors().remove(doctor)) {
            throw new BusinessRuleException("Doctor " + doctorId + " is not assigned to '" + department.getName() + "'");
        }
        doctor.getDepartments().remove(department);
        return department;
    }

    @Transactional
    public Department changeHeadDoctor(Long departmentId, Long doctorId) {
        Department department = getDepartment(departmentId);
        Doctor newHead = findDoctor(doctorId);
        ensureNotHeadElsewhere(newHead, departmentId);

        department.setHeadDoctor(newHead);
        if (department.getDoctors().add(newHead)) {
            newHead.getDepartments().add(department);
        }
        return department;
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = getDepartment(id);
        for (Doctor doctor : department.getDoctors()) {
            doctor.getDepartments().remove(department);
        }
        departmentRepository.delete(department); // join table rows go with the owning side
    }

    private Doctor findDoctor(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
    }

    private void ensureNotHeadElsewhere(Doctor doctor, Long allowedDepartmentId) {
        departmentRepository.findByHeadDoctorId(doctor.getId())
                .filter(d -> !d.getId().equals(allowedDepartmentId))
                .ifPresent(d -> {
                    throw new BusinessRuleException("Doctor " + doctor.getId() + " is already head of '" + d.getName() + "'");
                });
    }
}

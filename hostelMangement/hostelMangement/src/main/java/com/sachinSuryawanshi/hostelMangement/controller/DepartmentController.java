package com.sachinSuryawanshi.hostelMangement.controller;

import com.sachinSuryawanshi.hostelMangement.DTO.DepartmentRequestDto;
import com.sachinSuryawanshi.hostelMangement.DTO.DepartmentResponseDto;
import com.sachinSuryawanshi.hostelMangement.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public List<DepartmentResponseDto> getAllDepartments() {
        return departmentService.getAllDepartments().stream()
                .map(DepartmentResponseDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public DepartmentResponseDto getDepartment(@PathVariable Long id) {
        return DepartmentResponseDto.from(departmentService.getDepartment(id));
    }

    @PostMapping
    public ResponseEntity<DepartmentResponseDto> createDepartment(@RequestBody DepartmentRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DepartmentResponseDto.from(departmentService.createDepartment(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/doctors/{doctorId}")
    public DepartmentResponseDto addDoctor(@PathVariable Long id, @PathVariable Long doctorId) {
        return DepartmentResponseDto.from(departmentService.addDoctorToDepartment(id, doctorId));
    }

    @DeleteMapping("/{id}/doctors/{doctorId}")
    public DepartmentResponseDto removeDoctor(@PathVariable Long id, @PathVariable Long doctorId) {
        return DepartmentResponseDto.from(departmentService.removeDoctorFromDepartment(id, doctorId));
    }

    @PutMapping("/{id}/head/{doctorId}")
    public DepartmentResponseDto changeHead(@PathVariable Long id, @PathVariable Long doctorId) {
        return DepartmentResponseDto.from(departmentService.changeHeadDoctor(id, doctorId));
    }
}

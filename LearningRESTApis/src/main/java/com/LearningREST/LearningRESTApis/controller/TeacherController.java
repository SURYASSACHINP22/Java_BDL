package com.LearningREST.LearningRESTApis.controller;

import com.LearningREST.LearningRESTApis.DTO.TeacherDto;
import com.LearningREST.LearningRESTApis.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teacher")
public class TeacherController {
    private final TeacherService teacherService;

    @PostMapping
    public ResponseEntity<TeacherDto> createNewTeacher(@RequestBody TeacherDto teacherDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teacherService.createNewTeacher(teacherDto));
    }

    @GetMapping
    public ResponseEntity<List<TeacherDto>> getAllTeachers(){
        return ResponseEntity.ok(teacherService.getAllTeachers());
    }
}

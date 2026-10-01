package com.LearningREST.LearningRESTApis.service;

import com.LearningREST.LearningRESTApis.DTO.AddStudentRequestDto;
import com.LearningREST.LearningRESTApis.DTO.StudentDto;

import java.util.List;
import java.util.Map;

public interface StudentService {
    void deleteStudentById(Long id);

    StudentDto getStudentByName(String name);

    List<StudentDto> getAllStudents();

    StudentDto getStudentById(long id);

    StudentDto createNewStudent(AddStudentRequestDto addStudentRequestDto);

    StudentDto updateStudent(Long id, AddStudentRequestDto addStudentRequestDto);

    StudentDto updateStudentPatch(Long id, Map<String, Object> updates);
}

package com.LearningREST.LearningRESTApis.service;

import com.LearningREST.LearningRESTApis.DTO.TeacherDto;

import java.util.List;

public interface TeacherService {
    TeacherDto createNewTeacher(TeacherDto teacherDto);

    List<TeacherDto> getAllTeachers();
}

package com.LearningREST.LearningRESTApis.service.impl;

import com.LearningREST.LearningRESTApis.DTO.TeacherDto;
import com.LearningREST.LearningRESTApis.entity.Teacher;
import com.LearningREST.LearningRESTApis.repository.TeacherRepository;
import com.LearningREST.LearningRESTApis.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {
    private final TeacherRepository teacherRepository;
    private final ModelMapper modelMapper;


    @Override
    public TeacherDto createNewTeacher(TeacherDto teacherDto) {
        Teacher teacher = modelMapper.map(teacherDto, Teacher.class);
        Teacher newTeacher = teacherRepository.save(teacher);
        return modelMapper.map(newTeacher, TeacherDto.class);
    }

    @Override
    public List<TeacherDto> getAllTeachers() {
        return teacherRepository.findAll()
                .stream()
                .map(teacher -> modelMapper.map(teacher, TeacherDto.class))
                .toList();
    }
}

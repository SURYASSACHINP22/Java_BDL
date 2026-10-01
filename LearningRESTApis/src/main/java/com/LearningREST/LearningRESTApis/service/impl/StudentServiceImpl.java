package com.LearningREST.LearningRESTApis.service.impl;

import com.LearningREST.LearningRESTApis.DTO.AddStudentRequestDto;
import com.LearningREST.LearningRESTApis.DTO.StudentDto;
import com.LearningREST.LearningRESTApis.entity.Student;
import com.LearningREST.LearningRESTApis.exception.ResourceNotFoundException;
import com.LearningREST.LearningRESTApis.repository.StudentRepository;
import com.LearningREST.LearningRESTApis.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;

    @Override
    public void deleteStudentById(Long id) {
        if(!studentRepository.existsById(id)){
            throw new ResourceNotFoundException("student not found with the id:"+id);
        }
        studentRepository.deleteById(id);
    }

    @Override
    public StudentDto getStudentByName(String name) {

        List<Student> students =
                (List<Student>) studentRepository.findAll();

        for (Student student : students) {

            if (name.equalsIgnoreCase(student.getName())) {

                return new StudentDto(
                        student.getId(),
                        student.getName(),
                        student.getEmail()
                );
            }
        }

        throw new ResourceNotFoundException("student not found with the name:"+name);
    }

    @Override
    public List<StudentDto> getAllStudents() {
        List<Student> students = (List<Student>) studentRepository.findAll();

        return students.stream()
                .map(student -> new StudentDto(
                        student.getId(),
                        student.getName(),
                        student.getEmail()))
                .toList();
    }

    @Override
    public StudentDto getStudentById(long id) {
        Student student = studentRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("student not found with the id:"+id));
        return new StudentDto(id, student.getName(), student.getEmail());
    }

    @Override
    public StudentDto createNewStudent(AddStudentRequestDto addStudentRequestDto) {
        Student newstudent = modelMapper.map(addStudentRequestDto, Student.class);
        Student student = studentRepository.save(newstudent);
        return modelMapper.map(student, StudentDto.class);
    }

    @Override
    public StudentDto updateStudent(Long id,AddStudentRequestDto addStudentRequestDto) {
        Student student1 = studentRepository.findById(id).
                orElseThrow(()-> new ResourceNotFoundException("student not found with the id:"+id));

        modelMapper.map(addStudentRequestDto, student1);
        Student updatedStudent = studentRepository.save(student1);
        return modelMapper.map(updatedStudent, StudentDto.class);
    }

    @Override
    public StudentDto updateStudentPatch(Long id, Map<String, Object> updates) {
        Student student = studentRepository.findById(id).
                orElseThrow(()-> new ResourceNotFoundException("student not found with the id:"+id));

//        modelMapper.map(updates, student);
//        Student updatedStudent = studentRepository.save(student);
//        return modelMapper.map(updatedStudent, StudentDto.class);

        updates.forEach((k,v)->{
           switch (k){
               case "name": student.setName((String) v) ;
               break;
               case "email": student.setEmail((String) v) ;
               break;
               default:
                   throw new RuntimeException("unknown key");
           }
        });
        studentRepository.save(student);
        return modelMapper.map(student, StudentDto.class);
    }

//    @Override
//    public StudentDto getStudentById(long id) {
//        Student student = studentRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("student not found with the id:"+id));
//        return modelMapper.map(student, StudentDto.class);
//    }
// same with above with modle mapper
}














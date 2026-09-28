package com.LearningREST.LearningRESTApis.controller;

import com.LearningREST.LearningRESTApis.DTO.AddStudentRequestDto;
import com.LearningREST.LearningRESTApis.DTO.StudentDto;
import com.LearningREST.LearningRESTApis.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("student")
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public List<StudentDto> getStudents() {
        return studentService.getAllStudents();
    }

    @GetMapping("/{id}")
    public StudentDto getStudentById(@PathVariable Long id) {
        return studentService.getStudentById(id);
    }

    @GetMapping("/name/{name}")
    public StudentDto getStudentByName(@PathVariable String name) {
        return studentService.getStudentByName(name);
    }

    @PostMapping
    public ResponseEntity<StudentDto> createNewStudent(@RequestBody  AddStudentRequestDto addStudentRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createNewStudent(addStudentRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudentById(@PathVariable Long id) {
        studentService.deleteStudentById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentDto> updateStudent(@PathVariable Long id, @RequestBody AddStudentRequestDto addStudentRequestDto) {
        return ResponseEntity.ok(studentService.updateStudent(id, addStudentRequestDto));
    }


//    private final StudentRepository studentRepository;
//    public StudentController(StudentService studentService, StudentRepository studentRepository) {
//        this.studentService = studentService;
//        this.studentRepository = studentRepository;
//    }

//    @GetMapping("/student")
//    public List<Student> student(){
//        return (List<Student>) studentRepository.findAll();
//    }


//    @GetMapping("student")
//    public StudentDto student(){
//        return new StudentDto(10,"sachin","sachin@gmail.com");
//    }
}

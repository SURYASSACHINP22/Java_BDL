package com.LearningREST.LearningRESTApis.repository;

import com.LearningREST.LearningRESTApis.entity.Student;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends CrudRepository<Student, Long> {
}

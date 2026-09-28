package com.LearningREST.LearningRESTApis.repository;

import com.LearningREST.LearningRESTApis.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
}

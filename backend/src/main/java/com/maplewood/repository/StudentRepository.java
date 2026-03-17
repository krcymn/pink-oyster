package com.maplewood.repository;

import com.maplewood.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByGradeLevel(Integer gradeLevel);

    List<Student> findByStatus(String status);
}
package com.maplewood.repository;

import com.maplewood.model.StudentCourseHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentCourseHistoryRepository extends JpaRepository<StudentCourseHistory, Long> {

    @EntityGraph(attributePaths = {"course", "semester"})
    List<StudentCourseHistory> findByStudentId(Long studentId);

    @Query("SELECT h FROM StudentCourseHistory h WHERE h.student.id = :studentId AND h.status = 'passed'")
    List<StudentCourseHistory> findPassedCoursesByStudentId(@Param("studentId") Long studentId);

    @Query("""
        SELECT COUNT(h) > 0 FROM StudentCourseHistory h
        WHERE h.student.id = :studentId
        AND h.course.id = :courseId
        AND h.status = 'passed'
    """)
    boolean hasStudentPassedCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
package com.maplewood.repository;

import com.maplewood.model.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {

    List<CourseSection> findBySemester_Id(Long semesterId);

    List<CourseSection> findByCourseIdAndSemesterId(Long courseId, Long semesterId);

    @Query("""
        SELECT cs FROM CourseSection cs
        JOIN Enrollment e ON e.courseSection.id = cs.id
        WHERE e.student.id = :studentId
        AND cs.semester.id = :semesterId
    """)
    List<CourseSection> findEnrolledSectionsByStudentAndSemester(
            @Param("studentId") Long studentId,
            @Param("semesterId") Long semesterId);
}
package com.maplewood.repository;

import com.maplewood.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    long countByCourseSectionId(Long sectionId);

    @Query("""
        SELECT e FROM Enrollment e
        WHERE e.student.id = :studentId
        AND e.courseSection.semester.id = :semesterId
    """)
    List<Enrollment> findByStudentAndSemester(
            @Param("studentId") Long studentId,
            @Param("semesterId") Long semesterId);

    Optional<Enrollment> findByStudentIdAndCourseSectionId(Long studentId, Long sectionId);

    @Query("""
        SELECT COUNT(e) FROM Enrollment e
        WHERE e.student.id = :studentId
        AND e.courseSection.semester.id = :semesterId
    """)
    long countByStudentAndSemester(
            @Param("studentId") Long studentId,
            @Param("semesterId") Long semesterId);
}
package com.maplewood.repository;

import com.maplewood.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByGradeLevelMinLessThanEqualAndGradeLevelMaxGreaterThanEqual(
            Integer gradeLevel, Integer gradeLevel2);

    // (1=Fall, 2=Spring)
    List<Course> findBySemesterOrder(Integer semesterOrder);

    List<Course> findByGradeLevelMinLessThanEqualAndGradeLevelMaxGreaterThanEqualAndSemesterOrder(
            Integer gradeLevelMin, Integer gradeLevelMax, Integer semesterOrder);

    // (core / elective)
    List<Course> findByCourseType(String courseType);

    @Query("""
        SELECT c FROM Course c
        WHERE (:gradeLevel IS NULL OR (c.gradeLevelMin <= :gradeLevel AND c.gradeLevelMax >= :gradeLevel))
        AND (:semesterOrder IS NULL OR c.semesterOrder = :semesterOrder)
        AND (:courseType IS NULL OR c.courseType = :courseType)
    """)
    List<Course> findWithFilters(
            @Param("gradeLevel") Integer gradeLevel,
            @Param("semesterOrder") Integer semesterOrder,
            @Param("courseType") String courseType);
}
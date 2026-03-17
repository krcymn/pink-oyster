package com.maplewood.validation;

import com.maplewood.exception.EnrollmentException;
import com.maplewood.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class MaxCoursesValidatorTest {

    private MaxCoursesValidator validator;
    private CourseSection section;
    private Student student;

    @BeforeEach
    void setUp() {
        validator = new MaxCoursesValidator();

        Course course = new Course();
        course.setId(1L);
        course.setCode("MAT201");
        course.setCredits(new BigDecimal("1.0"));
        course.setGradeLevelMin(9);
        course.setGradeLevelMax(12);

        Classroom classroom = new Classroom();
        classroom.setCapacity(10);

        section = new CourseSection();
        section.setId(1L);
        section.setCourse(course);
        section.setClassroom(classroom);
        section.setDaysOfWeek("MON,WED,FRI");
        section.setStartTime("09:00");
        section.setEndTime("10:00");

        student = new Student();
        student.setId(1L);
        student.setGradeLevel(10);
    }

    private EnrollmentValidationContext context(long count) {
        return EnrollmentValidationContext.builder()
                .student(student)
                .section(section)
                .enrolledSections(List.of())
                .currentEnrollmentCount(count)
                .enrolledInSection(5)
                .build();
    }

    @Test
    @DisplayName("Zero courses — should pass")
    void zeroCourses_shouldPass() {
        assertThatCode(() -> validator.validate(context(0))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("One course — should pass")
    void oneCourse_shouldPass() {
        assertThatCode(() -> validator.validate(context(1))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Four courses — should pass")
    void fourCourses_shouldPass() {
        assertThatCode(() -> validator.validate(context(4))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Five courses (max) — should throw MAX_COURSES_REACHED")
    void fiveCourses_shouldThrow() {
        assertThatThrownBy(() -> validator.validate(context(5)))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("MAX_COURSES_REACHED");
    }

    @Test
    @DisplayName("Six courses — should throw MAX_COURSES_REACHED")
    void sixCourses_shouldThrow() {
        assertThatThrownBy(() -> validator.validate(context(6)))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("MAX_COURSES_REACHED");
    }

    @Test
    @DisplayName("Error message contains max limit")
    void errorMessage_containsMaxLimit() {
        assertThatThrownBy(() -> validator.validate(context(5)))
                .hasMessageContaining("5");
    }

    @Test
    @DisplayName("suggestedAction suggests dropping a course")
    void suggestedAction_suggestsDrop() {
        assertThatThrownBy(() -> validator.validate(context(5)))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getSuggestedAction())
                .asString()
                .isNotBlank();
    }
}
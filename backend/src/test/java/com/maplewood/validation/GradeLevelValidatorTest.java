package com.maplewood.validation;

import com.maplewood.exception.EnrollmentException;
import com.maplewood.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class GradeLevelValidatorTest {

    private GradeLevelValidator validator;
    private Student student;
    private CourseSection section;
    private Course course;

    @BeforeEach
    void setUp() {
        validator = new GradeLevelValidator();

        course = new Course();
        course.setId(1L);
        course.setCode("MAT201");
        course.setCredits(new BigDecimal("1.0"));
        course.setGradeLevelMin(10);
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

    private EnrollmentValidationContext context() {
        return EnrollmentValidationContext.builder()
                .student(student)
                .section(section)
                .enrolledSections(List.of())
                .currentEnrollmentCount(2)
                .enrolledInSection(5)
                .build();
    }

    @Test
    @DisplayName("Grade at min boundary — should pass")
    void gradeAtMin_shouldPass() {
        student.setGradeLevel(10);
        assertThatCode(() -> validator.validate(context())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Grade at max boundary — should pass")
    void gradeAtMax_shouldPass() {
        student.setGradeLevel(12);
        assertThatCode(() -> validator.validate(context())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Grade within range — should pass")
    void gradeWithinRange_shouldPass() {
        student.setGradeLevel(11);
        assertThatCode(() -> validator.validate(context())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Grade below min — should throw GRADE_LEVEL_MISMATCH")
    void gradeBelowMin_shouldThrow() {
        student.setGradeLevel(9);
        assertThatThrownBy(() -> validator.validate(context()))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("GRADE_LEVEL_MISMATCH");
    }

    @Test
    @DisplayName("Grade above max — should throw GRADE_LEVEL_MISMATCH")
    void gradeAboveMax_shouldThrow() {
        student.setGradeLevel(13);
        assertThatThrownBy(() -> validator.validate(context()))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("GRADE_LEVEL_MISMATCH");
    }

    @Test
    @DisplayName("Error message contains grade info")
    void errorMessage_containsGradeInfo() {
        student.setGradeLevel(9);
        assertThatThrownBy(() -> validator.validate(context()))
                .hasMessageContaining("10")
                .hasMessageContaining("12")
                .hasMessageContaining("9");
    }

    @Test
    @DisplayName("suggestedAction contains student grade")
    void suggestedAction_containsStudentGrade() {
        student.setGradeLevel(9);
        assertThatThrownBy(() -> validator.validate(context()))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getSuggestedAction())
                .asString()
                .contains("9");
    }
}
package com.maplewood.validation;

import com.maplewood.exception.EnrollmentException;
import com.maplewood.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ClassroomCapacityValidatorTest {

    private ClassroomCapacityValidator validator;
    private CourseSection section;
    private Student student;
    private Classroom classroom;

    @BeforeEach
    void setUp() {
        validator = new ClassroomCapacityValidator();

        Course course = new Course();
        course.setId(1L);
        course.setCode("MAT201");
        course.setCredits(new BigDecimal("1.0"));
        course.setGradeLevelMin(9);
        course.setGradeLevelMax(12);

        classroom = new Classroom();
        classroom.setId(1L);
        classroom.setName("Room-101");
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

    private EnrollmentValidationContext context(long enrolledInSection) {
        return EnrollmentValidationContext.builder()
                .student(student)
                .section(section)
                .enrolledSections(List.of())
                .currentEnrollmentCount(2)
                .enrolledInSection(enrolledInSection)
                .build();
    }

    @Test
    @DisplayName("Empty classroom — should pass")
    void emptyClassroom_shouldPass() {
        assertThatCode(() -> validator.validate(context(0))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("One below capacity — should pass")
    void oneBelowCapacity_shouldPass() {
        assertThatCode(() -> validator.validate(context(9))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("At capacity — should throw CLASSROOM_FULL")
    void atCapacity_shouldThrow() {
        assertThatThrownBy(() -> validator.validate(context(10)))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("CLASSROOM_FULL");
    }

    @Test
    @DisplayName("Over capacity — should throw CLASSROOM_FULL")
    void overCapacity_shouldThrow() {
        assertThatThrownBy(() -> validator.validate(context(11)))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("CLASSROOM_FULL");
    }

    @Test
    @DisplayName("Error message contains classroom name and capacity")
    void errorMessage_containsRoomInfo() {
        assertThatThrownBy(() -> validator.validate(context(10)))
                .hasMessageContaining("Room-101")
                .hasMessageContaining("10");
    }

    @Test
    @DisplayName("suggestedAction suggests different section")
    void suggestedAction_suggestsDifferentSection() {
        assertThatThrownBy(() -> validator.validate(context(10)))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getSuggestedAction())
                .asString()
                .isNotBlank();
    }

    @Test
    @DisplayName("Capacity 1 — first student passes, second throws")
    void capacityOne_firstPassesSecondThrows() {
        classroom.setCapacity(1);
        assertThatCode(() -> validator.validate(context(0))).doesNotThrowAnyException();
        assertThatThrownBy(() -> validator.validate(context(1)))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("CLASSROOM_FULL");
    }
}
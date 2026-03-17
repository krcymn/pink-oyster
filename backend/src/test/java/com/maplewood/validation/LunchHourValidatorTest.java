package com.maplewood.validation;

import com.maplewood.exception.EnrollmentException;
import com.maplewood.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class LunchHourValidatorTest {

    private LunchHourValidator validator;
    private CourseSection section;
    private Student student;

    @BeforeEach
    void setUp() {
        validator = new LunchHourValidator();

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
    @DisplayName("Morning class — should pass")
    void morningClass_shouldPass() {
        section.setStartTime("08:00");
        section.setEndTime("09:00");
        assertThatCode(() -> validator.validate(context())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Ends exactly at lunch start (12:00) — should pass")
    void endsAtLunchStart_shouldPass() {
        section.setStartTime("11:00");
        section.setEndTime("12:00");
        assertThatCode(() -> validator.validate(context())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Starts exactly at lunch end (13:00) — should pass")
    void startsAtLunchEnd_shouldPass() {
        section.setStartTime("13:00");
        section.setEndTime("14:00");
        assertThatCode(() -> validator.validate(context())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Afternoon class — should pass")
    void afternoonClass_shouldPass() {
        section.setStartTime("14:00");
        section.setEndTime("15:00");
        assertThatCode(() -> validator.validate(context())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Overlaps lunch start — should throw LUNCH_HOUR_CONFLICT")
    void overlapsLunchStart_shouldThrow() {
        section.setStartTime("11:30");
        section.setEndTime("12:30");
        assertThatThrownBy(() -> validator.validate(context()))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("LUNCH_HOUR_CONFLICT");
    }

    @Test
    @DisplayName("Overlaps lunch end — should throw LUNCH_HOUR_CONFLICT")
    void overlapsLunchEnd_shouldThrow() {
        section.setStartTime("12:30");
        section.setEndTime("13:30");
        assertThatThrownBy(() -> validator.validate(context()))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("LUNCH_HOUR_CONFLICT");
    }

    @Test
    @DisplayName("Entirely during lunch — should throw LUNCH_HOUR_CONFLICT")
    void entirelyDuringLunch_shouldThrow() {
        section.setStartTime("12:00");
        section.setEndTime("13:00");
        assertThatThrownBy(() -> validator.validate(context()))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("LUNCH_HOUR_CONFLICT");
    }

    @Test
    @DisplayName("Spans entire lunch hour — should throw LUNCH_HOUR_CONFLICT")
    void spansEntireLunch_shouldThrow() {
        section.setStartTime("11:00");
        section.setEndTime("14:00");
        assertThatThrownBy(() -> validator.validate(context()))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("LUNCH_HOUR_CONFLICT");
    }
}
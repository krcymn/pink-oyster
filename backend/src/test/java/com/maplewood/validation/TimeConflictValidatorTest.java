package com.maplewood.validation;

import com.maplewood.exception.EnrollmentException;
import com.maplewood.model.Course;
import com.maplewood.model.CourseSection;
import com.maplewood.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class TimeConflictValidatorTest {

    private TimeConflictValidator validator;
    private CourseSection incoming;
    private Student student;

    @BeforeEach
    void setUp() {
        validator = new TimeConflictValidator();

        Course course = new Course();
        course.setId(1L);
        course.setCode("MAT201");

        incoming = new CourseSection();
        incoming.setId(1L);
        incoming.setCourse(course);
        incoming.setDaysOfWeek("MON,WED,FRI");
        incoming.setStartTime("09:00");
        incoming.setEndTime("10:00");

        student = new Student();
        student.setId(1L);
        student.setGradeLevel(10);
    }

    private EnrollmentValidationContext contextWith(List<CourseSection> enrolledSections) {
        return EnrollmentValidationContext.builder()
                .student(student)
                .section(incoming)
                .enrolledSections(enrolledSections)
                .currentEnrollmentCount(2)
                .enrolledInSection(5)
                .build();
    }

    private CourseSection existingSection(String days, String start, String end) {
        Course c = new Course();
        c.setCode("ENG101");
        CourseSection s = new CourseSection();
        s.setCourse(c);
        s.setDaysOfWeek(days);
        s.setStartTime(start);
        s.setEndTime(end);
        return s;
    }

    @Test
    @DisplayName("No enrolled sections — no conflict")
    void noEnrolledSections_shouldPass() {
        assertThatCode(() -> validator.validate(contextWith(List.of())))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Exact same time slot — should throw TIME_CONFLICT")
    void exactSameTimeSlot_shouldThrow() {
        assertThatThrownBy(() -> validator.validate(
                contextWith(List.of(existingSection("MON,WED,FRI", "09:00", "10:00")))))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("TIME_CONFLICT");
    }

    @Test
    @DisplayName("Partial overlap — should throw TIME_CONFLICT")
    void partialOverlap_shouldThrow() {
        // 09:30 - 10:30 overlaps with 09:00 - 10:00
        assertThatThrownBy(() -> validator.validate(
                contextWith(List.of(existingSection("MON,WED,FRI", "09:30", "10:30")))))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("TIME_CONFLICT");
    }

    @Test
    @DisplayName("Back to back — no overlap — should pass")
    void backToBack_shouldPass() {
        // 10:00 - 11:00 immediately after 09:00 - 10:00
        assertThatCode(() -> validator.validate(
                contextWith(List.of(existingSection("MON,WED,FRI", "10:00", "11:00")))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Different days — no conflict")
    void differentDays_shouldPass() {
        assertThatCode(() -> validator.validate(
                contextWith(List.of(existingSection("TUE,THU", "09:00", "10:00")))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("One common day with overlap — should throw")
    void oneCommonDayWithOverlap_shouldThrow() {
        // MON,WED,FRI vs MON,TUE — MON is common, times overlap
        assertThatThrownBy(() -> validator.validate(
                contextWith(List.of(existingSection("MON,TUE", "09:00", "10:00")))))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("TIME_CONFLICT");
    }
}
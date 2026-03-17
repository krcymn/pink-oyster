package com.maplewood.validation;

import com.maplewood.exception.EnrollmentException;
import com.maplewood.model.*;
import com.maplewood.repository.StudentCourseHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrerequisiteValidatorTest {

    @Mock private StudentCourseHistoryRepository historyRepository;

    private PrerequisiteValidator validator;
    private CourseSection section;
    private Course course;
    private Student student;

    @BeforeEach
    void setUp() {
        validator = new PrerequisiteValidator(historyRepository);

        course = new Course();
        course.setId(10L);
        course.setCode("MAT201");
        course.setName("Algebra II");
        course.setCredits(new BigDecimal("1.0"));
        course.setGradeLevelMin(9);
        course.setGradeLevelMax(12);
        course.setPrerequisite(null);

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

    private Course prereq(Long id, String code, String name) {
        Course p = new Course();
        p.setId(id);
        p.setCode(code);
        p.setName(name);
        return p;
    }

    @Test
    @DisplayName("No prerequisite — should pass without DB call")
    void noPrerequisite_shouldPassWithoutDbCall() {
        course.setPrerequisite(null);
        assertThatCode(() -> validator.validate(context())).doesNotThrowAnyException();
        verifyNoInteractions(historyRepository);
    }

    @Test
    @DisplayName("Prerequisite met — should pass")
    void prerequisiteMet_shouldPass() {
        course.setPrerequisite(prereq(5L, "MAT101", "Algebra I"));
        when(historyRepository.hasStudentPassedCourse(1L, 5L)).thenReturn(true);
        assertThatCode(() -> validator.validate(context())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Prerequisite not met — should throw PREREQ_MISSING")
    void prerequisiteNotMet_shouldThrow() {
        course.setPrerequisite(prereq(5L, "MAT101", "Algebra I"));
        when(historyRepository.hasStudentPassedCourse(1L, 5L)).thenReturn(false);
        assertThatThrownBy(() -> validator.validate(context()))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("PREREQ_MISSING");
    }

    @Test
    @DisplayName("Error message contains prerequisite code and name")
    void errorMessage_containsPrereqInfo() {
        course.setPrerequisite(prereq(5L, "MAT101", "Algebra I"));
        when(historyRepository.hasStudentPassedCourse(1L, 5L)).thenReturn(false);
        assertThatThrownBy(() -> validator.validate(context()))
                .hasMessageContaining("MAT101")
                .hasMessageContaining("Algebra I");
    }

    @Test
    @DisplayName("suggestedAction contains prerequisite course ID")
    void suggestedAction_containsPrereqId() {
        course.setPrerequisite(prereq(5L, "MAT101", "Algebra I"));
        when(historyRepository.hasStudentPassedCourse(1L, 5L)).thenReturn(false);
        assertThatThrownBy(() -> validator.validate(context()))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getSuggestedAction())
                .asString()
                .contains("5");
    }

    @Test
    @DisplayName("Repository called with correct student and course ids")
    void repositoryCalledWithCorrectIds() {
        course.setPrerequisite(prereq(5L, "MAT101", "Algebra I"));
        when(historyRepository.hasStudentPassedCourse(1L, 5L)).thenReturn(true);
        validator.validate(context());
        verify(historyRepository).hasStudentPassedCourse(1L, 5L);
    }
}
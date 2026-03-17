package com.maplewood.validation;

import com.maplewood.exception.EnrollmentException;
import com.maplewood.model.*;
import com.maplewood.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlreadyEnrolledValidatorTest {

    @Mock private EnrollmentRepository enrollmentRepository;

    private AlreadyEnrolledValidator validator;
    private CourseSection section;
    private Student student;

    @BeforeEach
    void setUp() {
        validator = new AlreadyEnrolledValidator(enrollmentRepository);

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
    @DisplayName("Not enrolled — should pass")
    void notEnrolled_shouldPass() {
        when(enrollmentRepository.findByStudentIdAndCourseSectionId(1L, 1L))
                .thenReturn(Optional.empty());
        assertThatCode(() -> validator.validate(context())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Already enrolled — should throw ALREADY_ENROLLED")
    void alreadyEnrolled_shouldThrow() {
        when(enrollmentRepository.findByStudentIdAndCourseSectionId(1L, 1L))
                .thenReturn(Optional.of(new Enrollment()));
        assertThatThrownBy(() -> validator.validate(context()))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("ALREADY_ENROLLED");
    }

    @Test
    @DisplayName("Repository called with correct ids")
    void repositoryCalledWithCorrectIds() {
        when(enrollmentRepository.findByStudentIdAndCourseSectionId(1L, 1L))
                .thenReturn(Optional.empty());
        validator.validate(context());
        verify(enrollmentRepository).findByStudentIdAndCourseSectionId(1L, 1L);
    }

    @Test
    @DisplayName("suggestedAction is not blank")
    void suggestedAction_notBlank() {
        when(enrollmentRepository.findByStudentIdAndCourseSectionId(1L, 1L))
                .thenReturn(Optional.of(new Enrollment()));
        assertThatThrownBy(() -> validator.validate(context()))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getSuggestedAction())
                .asString()
                .isNotBlank();
    }
}
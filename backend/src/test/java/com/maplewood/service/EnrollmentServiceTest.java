package com.maplewood.service;

import com.maplewood.dto.EnrollmentDTO;
import com.maplewood.exception.EnrollmentException;
import com.maplewood.model.*;
import com.maplewood.repository.*;
import com.maplewood.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private CourseSectionRepository sectionRepository;
    @Mock private SemesterRepository semesterRepository;
    @Mock private CourseSectionService sectionService;

    // Validators
    @Mock private StudentCourseHistoryRepository historyRepository;

    private EnrollmentService enrollmentService;

    private Student student;
    private CourseSection section;
    private Course course;
    private Semester semester;
    private Classroom classroom;

    @BeforeEach
    void setUp() {
        // Validator'ları gerçek instance olarak oluştur
        List<EnrollmentValidator> validators = List.of(
                new GradeLevelValidator(),
                new LunchHourValidator(),
                new MaxCoursesValidator(),
                new AlreadyEnrolledValidator(enrollmentRepository),
                new ClassroomCapacityValidator(),
                new PrerequisiteValidator(historyRepository),
                new TimeConflictValidator()
        );

        enrollmentService = new EnrollmentService(
                enrollmentRepository,
                studentRepository,
                sectionRepository,
                semesterRepository,
                sectionService,
                validators
        );

        // Test verilerini hazırla
        semester = new Semester();
        semester.setId(7L);
        semester.setName("Fall");
        semester.setYear(2024);
        semester.setIsActive(true);

        classroom = new Classroom();
        classroom.setId(1L);
        classroom.setName("Room-101");
        classroom.setCapacity(10);

        course = new Course();
        course.setId(10L);
        course.setCode("MAT201");
        course.setName("Algebra II");
        course.setCredits(new BigDecimal("1.0"));
        course.setGradeLevelMin(10);
        course.setGradeLevelMax(12);
        course.setPrerequisite(null);

        section = new CourseSection();
        section.setId(1L);
        section.setCourse(course);
        section.setSemester(semester);
        section.setClassroom(classroom);
        section.setDaysOfWeek("MON,WED,FRI");
        section.setStartTime("09:00");
        section.setEndTime("10:00");

        student = new Student();
        student.setId(1L);
        student.setFirstName("Emma");
        student.setLastName("Wilson");
        student.setGradeLevel(10);
    }

    // ─── HAPPY PATH ───────────────────────────────────────────

    @Test
    @DisplayName("Valid enrollment — should succeed")
    void enroll_validRequest_shouldSucceed() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.of(semester));
        when(enrollmentRepository.findByStudentIdAndCourseSectionId(1L, 1L)).thenReturn(Optional.empty());
        when(enrollmentRepository.countByStudentAndSemester(1L, 7L)).thenReturn(2L);
        when(enrollmentRepository.countByCourseSectionId(1L)).thenReturn(5L);
        when(sectionRepository.findEnrolledSectionsByStudentAndSemester(1L, 7L)).thenReturn(List.of());
        when(enrollmentRepository.save(any())).thenAnswer(inv -> {
            Enrollment e = inv.getArgument(0);
            e.setId(100L);
            return e;
        });
        when(sectionService.getSectionById(1L)).thenReturn(null);

        EnrollmentDTO.EnrollmentResponse response = enrollmentService.enroll(1L, 1L);

        assertThat(response.getStudentId()).isEqualTo(1L);
        assertThat(response.getStudentFullName()).isEqualTo("Emma Wilson");
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    // ─── VALIDASYON 1: GRADE LEVEL ────────────────────────────

    @Test
    @DisplayName("Grade level too low — should throw GRADE_LEVEL_MISMATCH")
    void enroll_gradeLevelTooLow_shouldThrow() {
        student.setGradeLevel(9); // course min=10
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.of(semester));

        assertThatThrownBy(() -> enrollmentService.enroll(1L, 1L))
                .isInstanceOf(EnrollmentException.class)
                .hasMessageContaining("grade levels")
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("GRADE_LEVEL_MISMATCH");
    }

    @Test
    @DisplayName("Grade level too high — should throw GRADE_LEVEL_MISMATCH")
    void enroll_gradeLevelTooHigh_shouldThrow() {
        course.setGradeLevelMax(10);
        student.setGradeLevel(11);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.of(semester));

        assertThatThrownBy(() -> enrollmentService.enroll(1L, 1L))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("GRADE_LEVEL_MISMATCH");
    }

    // ─── VALIDASYON 2: LUNCH HOUR ─────────────────────────────

    @Test
    @DisplayName("Lunch hour conflict — should throw LUNCH_HOUR_CONFLICT")
    void enroll_lunchHourConflict_shouldThrow() {
        section.setStartTime("11:30");
        section.setEndTime("12:30");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.of(semester));

        assertThatThrownBy(() -> enrollmentService.enroll(1L, 1L))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("LUNCH_HOUR_CONFLICT");
    }

    // ─── VALIDASYON 3: MAX COURSES ────────────────────────────

    @Test
    @DisplayName("Max courses reached — should throw MAX_COURSES_REACHED")
    void enroll_maxCoursesReached_shouldThrow() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.of(semester));
        when(enrollmentRepository.countByStudentAndSemester(1L, 7L)).thenReturn(5L);

        assertThatThrownBy(() -> enrollmentService.enroll(1L, 1L))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("MAX_COURSES_REACHED");
    }

    // ─── VALIDASYON 4: ALREADY ENROLLED ──────────────────────

    @Test
    @DisplayName("Already enrolled — should throw ALREADY_ENROLLED")
    void enroll_alreadyEnrolled_shouldThrow() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.of(semester));
        when(enrollmentRepository.countByStudentAndSemester(1L, 7L)).thenReturn(2L);
        when(enrollmentRepository.findByStudentIdAndCourseSectionId(1L, 1L))
                .thenReturn(Optional.of(new Enrollment()));

        assertThatThrownBy(() -> enrollmentService.enroll(1L, 1L))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("ALREADY_ENROLLED");
    }

    // ─── VALIDASYON 5: CLASSROOM CAPACITY ────────────────────

    @Test
    @DisplayName("Classroom full — should throw CLASSROOM_FULL")
    void enroll_classroomFull_shouldThrow() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.of(semester));
        when(enrollmentRepository.countByStudentAndSemester(1L, 7L)).thenReturn(2L);
        when(enrollmentRepository.findByStudentIdAndCourseSectionId(1L, 1L)).thenReturn(Optional.empty());
        when(enrollmentRepository.countByCourseSectionId(1L)).thenReturn(10L); // capacity=10, full!

        assertThatThrownBy(() -> enrollmentService.enroll(1L, 1L))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("CLASSROOM_FULL");
    }

    // ─── VALIDASYON 6: PREREQUISITE ──────────────────────────

    @Test
    @DisplayName("Prerequisite not met — should throw PREREQ_MISSING")
    void enroll_prerequisiteNotMet_shouldThrow() {
        Course prereq = new Course();
        prereq.setId(5L);
        prereq.setCode("MAT101");
        prereq.setName("Algebra I");
        course.setPrerequisite(prereq);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.of(semester));
        when(enrollmentRepository.countByStudentAndSemester(1L, 7L)).thenReturn(2L);
        when(enrollmentRepository.findByStudentIdAndCourseSectionId(1L, 1L)).thenReturn(Optional.empty());
        when(enrollmentRepository.countByCourseSectionId(1L)).thenReturn(5L);
        when(historyRepository.hasStudentPassedCourse(1L, 5L)).thenReturn(false);

        assertThatThrownBy(() -> enrollmentService.enroll(1L, 1L))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("PREREQ_MISSING");
    }

    @Test
    @DisplayName("Prerequisite met — should pass validation")
    void enroll_prerequisiteMet_shouldPass() {
        Course prereq = new Course();
        prereq.setId(5L);
        prereq.setCode("MAT101");
        prereq.setName("Algebra I");
        course.setPrerequisite(prereq);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.of(semester));
        when(enrollmentRepository.countByStudentAndSemester(1L, 7L)).thenReturn(2L);
        when(enrollmentRepository.findByStudentIdAndCourseSectionId(1L, 1L)).thenReturn(Optional.empty());
        when(enrollmentRepository.countByCourseSectionId(1L)).thenReturn(5L);
        when(historyRepository.hasStudentPassedCourse(1L, 5L)).thenReturn(true);
        when(sectionRepository.findEnrolledSectionsByStudentAndSemester(1L, 7L)).thenReturn(List.of());
        when(enrollmentRepository.save(any())).thenAnswer(inv -> {
            Enrollment e = inv.getArgument(0);
            e.setId(100L);
            return e;
        });
        when(sectionService.getSectionById(1L)).thenReturn(null);

        assertThatCode(() -> enrollmentService.enroll(1L, 1L)).doesNotThrowAnyException();
    }

    // ─── VALIDASYON 7: TIME CONFLICT ─────────────────────────

    @Test
    @DisplayName("Time conflict — same days and overlapping hours — should throw TIME_CONFLICT")
    void enroll_timeConflict_shouldThrow() {
        Course existingCourse = new Course();
        existingCourse.setId(20L);
        existingCourse.setCode("ENG101");

        CourseSection existingSection = new CourseSection();
        existingSection.setId(2L);
        existingSection.setCourse(existingCourse);
        existingSection.setDaysOfWeek("MON,WED,FRI");
        existingSection.setStartTime("09:00");
        existingSection.setEndTime("10:00"); // tam çakışma

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.of(semester));
        when(enrollmentRepository.countByStudentAndSemester(1L, 7L)).thenReturn(2L);
        when(enrollmentRepository.findByStudentIdAndCourseSectionId(1L, 1L)).thenReturn(Optional.empty());
        when(enrollmentRepository.countByCourseSectionId(1L)).thenReturn(5L);
        when(sectionRepository.findEnrolledSectionsByStudentAndSemester(1L, 7L))
                .thenReturn(List.of(existingSection));

        assertThatThrownBy(() -> enrollmentService.enroll(1L, 1L))
                .isInstanceOf(EnrollmentException.class)
                .extracting(e -> ((EnrollmentException) e).getErrorCode())
                .isEqualTo("TIME_CONFLICT");
    }

    @Test
    @DisplayName("Different days — no time conflict — should pass")
    void enroll_differentDays_noConflict_shouldPass() {
        Course existingCourse = new Course();
        existingCourse.setId(20L);
        existingCourse.setCode("ENG101");

        CourseSection existingSection = new CourseSection();
        existingSection.setId(2L);
        existingSection.setCourse(existingCourse);
        existingSection.setDaysOfWeek("TUE,THU"); // farklı günler
        existingSection.setStartTime("09:00");
        existingSection.setEndTime("10:00");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.of(semester));
        when(enrollmentRepository.countByStudentAndSemester(1L, 7L)).thenReturn(2L);
        when(enrollmentRepository.findByStudentIdAndCourseSectionId(1L, 1L)).thenReturn(Optional.empty());
        when(enrollmentRepository.countByCourseSectionId(1L)).thenReturn(5L);
        when(sectionRepository.findEnrolledSectionsByStudentAndSemester(1L, 7L))
                .thenReturn(List.of(existingSection));
        when(enrollmentRepository.save(any())).thenAnswer(inv -> {
            Enrollment e = inv.getArgument(0);
            e.setId(100L);
            return e;
        });
        when(sectionService.getSectionById(1L)).thenReturn(null);

        assertThatCode(() -> enrollmentService.enroll(1L, 1L)).doesNotThrowAnyException();
    }

    // ─── UNENROLL ─────────────────────────────────────────────

    @Test
    @DisplayName("Unenroll — existing enrollment — should delete")
    void unenroll_existingEnrollment_shouldDelete() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(100L);

        when(enrollmentRepository.findByStudentIdAndCourseSectionId(1L, 1L))
                .thenReturn(Optional.of(enrollment));

        enrollmentService.unenroll(1L, 1L);

        verify(enrollmentRepository).delete(enrollment);
    }

    @Test
    @DisplayName("Unenroll — not enrolled — should throw EntityNotFoundException")
    void unenroll_notEnrolled_shouldThrow() {
        when(enrollmentRepository.findByStudentIdAndCourseSectionId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.unenroll(1L, 1L))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }
}
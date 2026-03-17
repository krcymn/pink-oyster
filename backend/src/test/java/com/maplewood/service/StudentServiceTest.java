package com.maplewood.service;

import com.maplewood.dto.StudentDTO;
import com.maplewood.mapper.StudentMapper;
import com.maplewood.model.Course;
import com.maplewood.model.Student;
import com.maplewood.model.StudentCourseHistory;
import com.maplewood.repository.StudentCourseHistoryRepository;
import com.maplewood.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private StudentCourseHistoryRepository historyRepository;
    @Mock private StudentMapper studentMapper;
    @Mock private GpaCalculator gpaCalculator;
    @InjectMocks private StudentService studentService;

    private Student student;
    private StudentDTO simpleDTO;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setFirstName("Emma");
        student.setLastName("Wilson");
        student.setEmail("emma@school.edu");
        student.setGradeLevel(10);
        student.setEnrollmentYear(2023);
        student.setExpectedGraduationYear(2027);
        student.setStatus("active");

        simpleDTO = StudentDTO.builder()
                .id(1L)
                .firstName("Emma")
                .lastName("Wilson")
                .email("emma@school.edu")
                .gradeLevel(10)
                .build();
    }

    private StudentCourseHistory history(String status, double credits) {
        Course course = new Course();
        course.setCredits(new BigDecimal(String.valueOf(credits)));

        StudentCourseHistory h = new StudentCourseHistory();
        h.setCourse(course);
        h.setStatus(status);
        return h;
    }

    @Test
    @DisplayName("getAllStudents — returns list of simple DTOs")
    void getAllStudents_returnsSimpleDTOs() {
        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(studentMapper.toSimpleDTO(student)).thenReturn(simpleDTO);

        List<StudentDTO> result = studentService.getAllStudents();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Emma");
    }

    @Test
    @DisplayName("getAllStudents — empty — returns empty list")
    void getAllStudents_empty_returnsEmpty() {
        when(studentRepository.findAll()).thenReturn(List.of());
        assertThat(studentService.getAllStudents()).isEmpty();
    }

    @Test
    @DisplayName("getStudentProfile — not found — throws EntityNotFoundException")
    void getStudentProfile_notFound_throws() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentProfile(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getStudentProfile — found — returns profile with GPA")
    void getStudentProfile_found_returnsProfileWithGpa() {
        List<StudentCourseHistory> history = List.of(
                history("passed", 1.0),
                history("failed", 1.0)
        );

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(historyRepository.findByStudentId(1L)).thenReturn(history);
        when(gpaCalculator.calculateCreditsEarned(history)).thenReturn(1.0);
        when(gpaCalculator.calculateGpa(history)).thenReturn(2.0);
        when(gpaCalculator.getCreditsRequired()).thenReturn(30.0);
        when(gpaCalculator.isGraduationEligible(1.0)).thenReturn(false);
        when(studentMapper.toSimpleDTO(student)).thenReturn(simpleDTO);
        when(studentMapper.toHistoryItemDTO(any())).thenReturn(
                StudentDTO.CourseHistoryItemDTO.builder()
                        .courseCode("MAT101").status("passed").build());

        StudentDTO result = studentService.getStudentProfile(1L);

        assertThat(result.getGpa()).isEqualTo(2.0);
        assertThat(result.getCreditsEarned()).isEqualTo(1.0);
        assertThat(result.getCreditsRequired()).isEqualTo(30.0);
        assertThat(result.isGraduationEligible()).isFalse();
        assertThat(result.getCourseHistory()).hasSize(2);
    }

    @Test
    @DisplayName("getStudentProfile — no history — GPA is 0")
    void getStudentProfile_noHistory_gpaIsZero() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(historyRepository.findByStudentId(1L)).thenReturn(List.of());
        when(gpaCalculator.calculateCreditsEarned(List.of())).thenReturn(0.0);
        when(gpaCalculator.calculateGpa(List.of())).thenReturn(0.0);
        when(gpaCalculator.getCreditsRequired()).thenReturn(30.0);
        when(gpaCalculator.isGraduationEligible(0.0)).thenReturn(false);
        when(studentMapper.toSimpleDTO(student)).thenReturn(simpleDTO);

        StudentDTO result = studentService.getStudentProfile(1L);

        assertThat(result.getGpa()).isEqualTo(0.0);
        assertThat(result.getCourseHistory()).isEmpty();
    }

    @Test
    @DisplayName("getStudentProfile — graduation eligible when 30 credits")
    void getStudentProfile_thirtyCredits_eligibleForGraduation() {
        List<StudentCourseHistory> history = List.of(history("passed", 30.0));

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(historyRepository.findByStudentId(1L)).thenReturn(history);
        when(gpaCalculator.calculateCreditsEarned(history)).thenReturn(30.0);
        when(gpaCalculator.calculateGpa(history)).thenReturn(4.0);
        when(gpaCalculator.getCreditsRequired()).thenReturn(30.0);
        when(gpaCalculator.isGraduationEligible(30.0)).thenReturn(true);
        when(studentMapper.toSimpleDTO(student)).thenReturn(simpleDTO);
        when(studentMapper.toHistoryItemDTO(any())).thenReturn(
                StudentDTO.CourseHistoryItemDTO.builder()
                        .courseCode("MAT101").status("passed").build());

        StudentDTO result = studentService.getStudentProfile(1L);

        assertThat(result.isGraduationEligible()).isTrue();
    }
}
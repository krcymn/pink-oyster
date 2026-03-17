package com.maplewood.service;

import com.maplewood.dto.CourseDTO;
import com.maplewood.mapper.CourseMapper;
import com.maplewood.model.Course;
import com.maplewood.repository.CourseRepository;
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
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private CourseMapper courseMapper;
    @InjectMocks private CourseService courseService;

    private Course course;
    private CourseDTO courseDTO;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setId(1L);
        course.setCode("MAT201");
        course.setName("Algebra II");
        course.setCredits(new BigDecimal("1.0"));
        course.setGradeLevelMin(10);
        course.setGradeLevelMax(12);
        course.setSemesterOrder(1);

        courseDTO = CourseDTO.builder()
                .id(1L)
                .code("MAT201")
                .name("Algebra II")
                .credits(new BigDecimal("1.0"))
                .build();
    }

    @Test
    @DisplayName("getAllCourses — returns mapped DTOs")
    void getAllCourses_returnsMappedDTOs() {
        when(courseRepository.findAll()).thenReturn(List.of(course));
        when(courseMapper.toDTO(course)).thenReturn(courseDTO);

        List<CourseDTO> result = courseService.getAllCourses();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("MAT201");
        verify(courseRepository).findAll();
    }

    @Test
    @DisplayName("getAllCourses — empty list — returns empty")
    void getAllCourses_empty_returnsEmpty() {
        when(courseRepository.findAll()).thenReturn(List.of());
        assertThat(courseService.getAllCourses()).isEmpty();
    }

    @Test
    @DisplayName("getCourseById — found — returns DTO")
    void getCourseById_found_returnsDTO() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseMapper.toDTO(course)).thenReturn(courseDTO);

        CourseDTO result = courseService.getCourseById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("MAT201");
    }

    @Test
    @DisplayName("getCourseById — not found — throws EntityNotFoundException")
    void getCourseById_notFound_throws() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getCoursesWithFilters — all null filters — returns all")
    void getCoursesWithFilters_nullFilters_returnsAll() {
        when(courseRepository.findWithFilters(null, null, null)).thenReturn(List.of(course));
        when(courseMapper.toDTO(course)).thenReturn(courseDTO);

        List<CourseDTO> result = courseService.getCoursesWithFilters(null, null, null);

        assertThat(result).hasSize(1);
        verify(courseRepository).findWithFilters(null, null, null);
    }

    @Test
    @DisplayName("getCoursesWithFilters — with gradeLevel — passes to repository")
    void getCoursesWithFilters_withGradeLevel_passesToRepository() {
        when(courseRepository.findWithFilters(10, null, null)).thenReturn(List.of(course));
        when(courseMapper.toDTO(course)).thenReturn(courseDTO);

        courseService.getCoursesWithFilters(10, null, null);

        verify(courseRepository).findWithFilters(10, null, null);
    }

    @Test
    @DisplayName("getCoursesWithFilters — with all filters — passes to repository")
    void getCoursesWithFilters_allFilters_passesToRepository() {
        when(courseRepository.findWithFilters(10, 1, "core")).thenReturn(List.of());

        courseService.getCoursesWithFilters(10, 1, "core");

        verify(courseRepository).findWithFilters(10, 1, "core");
    }
}
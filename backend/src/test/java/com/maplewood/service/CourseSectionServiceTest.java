package com.maplewood.service;

import com.maplewood.dto.CourseSectionDTO;
import com.maplewood.mapper.CourseSectionMapper;
import com.maplewood.model.Course;
import com.maplewood.model.CourseSection;
import com.maplewood.model.Semester;
import com.maplewood.repository.CourseSectionRepository;
import com.maplewood.repository.SemesterRepository;
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
class CourseSectionServiceTest {

    @Mock private CourseSectionRepository sectionRepository;
    @Mock private SemesterRepository semesterRepository;
    @Mock private CourseSectionMapper sectionMapper;
    @InjectMocks private CourseSectionService sectionService;

    private Semester activeSemester;
    private CourseSection section;
    private CourseSectionDTO sectionDTO;

    @BeforeEach
    void setUp() {
        activeSemester = new Semester();
        activeSemester.setId(7L);
        activeSemester.setName("Fall");
        activeSemester.setYear(2024);
        activeSemester.setIsActive(true);

        Course course = new Course();
        course.setId(1L);
        course.setCode("MAT201");
        course.setCredits(new BigDecimal("1.0"));

        section = new CourseSection();
        section.setId(1L);
        section.setCourse(course);
        section.setSemester(activeSemester);

        sectionDTO = CourseSectionDTO.builder()
                .id(1L)
                .courseCode("MAT201")
                .semesterName("Fall")
                .build();
    }

    @Test
    @DisplayName("getActiveSemesterSections — returns sections for active semester")
    void getActiveSemesterSections_returnsActiveSections() {
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.of(activeSemester));
        when(sectionRepository.findBySemester_Id(7L)).thenReturn(List.of(section));
        when(sectionMapper.toDTO(section)).thenReturn(sectionDTO);

        List<CourseSectionDTO> result = sectionService.getActiveSemesterSections();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCourseCode()).isEqualTo("MAT201");
    }

    @Test
    @DisplayName("getActiveSemesterSections — no active semester — throws")
    void getActiveSemesterSections_noActiveSemester_throws() {
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.getActiveSemesterSections())
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("active semester");
    }

    @Test
    @DisplayName("getActiveSemesterSections — empty — returns empty list")
    void getActiveSemesterSections_empty_returnsEmpty() {
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.of(activeSemester));
        when(sectionRepository.findBySemester_Id(7L)).thenReturn(List.of());

        assertThat(sectionService.getActiveSemesterSections()).isEmpty();
    }

    @Test
    @DisplayName("getSectionById — found — returns DTO")
    void getSectionById_found_returnsDTO() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(sectionMapper.toDTO(section)).thenReturn(sectionDTO);

        CourseSectionDTO result = sectionService.getSectionById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getSectionById — not found — throws EntityNotFoundException")
    void getSectionById_notFound_throws() {
        when(sectionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.getSectionById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getSectionsByCourse — returns sections for course in active semester")
    void getSectionsByCourse_returnsSections() {
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.of(activeSemester));
        when(sectionRepository.findByCourseIdAndSemesterId(1L, 7L)).thenReturn(List.of(section));
        when(sectionMapper.toDTO(section)).thenReturn(sectionDTO);

        List<CourseSectionDTO> result = sectionService.getSectionsByCourse(1L);

        assertThat(result).hasSize(1);
        verify(sectionRepository).findByCourseIdAndSemesterId(1L, 7L);
    }

    @Test
    @DisplayName("getSectionsByCourse — course has no sections — returns empty")
    void getSectionsByCourse_noSections_returnsEmpty() {
        when(semesterRepository.findByIsActiveTrue()).thenReturn(Optional.of(activeSemester));
        when(sectionRepository.findByCourseIdAndSemesterId(99L, 7L)).thenReturn(List.of());

        assertThat(sectionService.getSectionsByCourse(99L)).isEmpty();
    }
}
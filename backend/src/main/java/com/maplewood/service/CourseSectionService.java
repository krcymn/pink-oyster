package com.maplewood.service;

import com.maplewood.dto.CourseSectionDTO;
import com.maplewood.mapper.CourseSectionMapper;
import com.maplewood.model.Semester;
import com.maplewood.repository.CourseSectionRepository;
import com.maplewood.repository.SemesterRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseSectionService {

    private final CourseSectionRepository sectionRepository;
    private final SemesterRepository semesterRepository;
    private final CourseSectionMapper sectionMapper;

    public List<CourseSectionDTO> getActiveSemesterSections() {
        return sectionRepository.findBySemester_Id(getActiveSemester().getId())
                .stream()
                .map(sectionMapper::toDTO)
                .toList();
    }

    public CourseSectionDTO getSectionById(Long id) {
        return sectionRepository.findById(id)
                .map(sectionMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Course section not found with id: " + id));
    }

    public List<CourseSectionDTO> getSectionsByCourse(Long courseId) {
        return sectionRepository.findByCourseIdAndSemesterId(courseId, getActiveSemester().getId())
                .stream()
                .map(sectionMapper::toDTO)
                .toList();
    }

    private Semester getActiveSemester() {
        return semesterRepository.findByIsActiveTrue()
                .orElseThrow(() -> new EntityNotFoundException("No active semester found"));
    }
}
package com.maplewood.service;

import com.maplewood.dto.CourseDTO;
import com.maplewood.mapper.CourseMapper;
import com.maplewood.model.Course;
import com.maplewood.repository.CourseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(courseMapper::toDTO)
                .toList();
    }

    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));
        return courseMapper.toDTO(course);
    }

    public List<CourseDTO> getCoursesWithFilters(Integer gradeLevel, Integer semesterOrder, String courseType) {
        return courseRepository.findWithFilters(gradeLevel, semesterOrder, courseType)
                .stream()
                .map(courseMapper::toDTO)
                .toList();
    }
}
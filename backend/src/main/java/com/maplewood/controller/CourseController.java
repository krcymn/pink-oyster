package com.maplewood.controller;

import com.maplewood.dto.CourseDTO;
import com.maplewood.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Course catalog management")
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "Get all courses", description = "Filter by gradeLevel, semesterOrder, courseType")
    @ApiResponse(responseCode = "200", description = "List of courses")
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getCourses(
            @RequestParam(required = false) Integer gradeLevel,
            @RequestParam(required = false) Integer semesterOrder,
            @RequestParam(required = false) String courseType) {
        return ResponseEntity.ok(courseService.getCoursesWithFilters(gradeLevel, semesterOrder, courseType));
    }

    @Operation(summary = "Get course by ID")
    @ApiResponse(responseCode = "200", description = "Course found")
    @ApiResponse(responseCode = "404", description = "Course not found")
    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }
}
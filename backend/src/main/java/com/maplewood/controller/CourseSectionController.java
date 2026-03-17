package com.maplewood.controller;

import com.maplewood.dto.CourseSectionDTO;
import com.maplewood.service.CourseSectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
@Tag(name = "Course Sections", description = "Active semester course sections with time slots")
public class CourseSectionController {

    private final CourseSectionService sectionService;

    @Operation(summary = "Get all active semester sections")
    @ApiResponse(responseCode = "200", description = "List of sections")
    @ApiResponse(responseCode = "404", description = "No active semester found")
    @GetMapping
    public ResponseEntity<List<CourseSectionDTO>> getActiveSections() {
        return ResponseEntity.ok(sectionService.getActiveSemesterSections());
    }

    @Operation(summary = "Get section by ID")
    @ApiResponse(responseCode = "200", description = "Section found")
    @ApiResponse(responseCode = "404", description = "Section not found")
    @GetMapping("/{id}")
    public ResponseEntity<CourseSectionDTO> getSectionById(@PathVariable Long id) {
        return ResponseEntity.ok(sectionService.getSectionById(id));
    }

    @Operation(summary = "Get sections by course")
    @ApiResponse(responseCode = "200", description = "Sections for the given course")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseSectionDTO>> getSectionsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(sectionService.getSectionsByCourse(courseId));
    }
}
package com.maplewood.controller;

import com.maplewood.dto.EnrollmentDTO;
import com.maplewood.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "Course enrollment with full validation")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Operation(summary = "Get student schedule", description = "Returns current semester schedule for a student")
    @ApiResponse(responseCode = "200", description = "Schedule found")
    @ApiResponse(responseCode = "404", description = "Student not found")
    @GetMapping("/schedule/{studentId}")
    public ResponseEntity<EnrollmentDTO.ScheduleResponse> getSchedule(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.getStudentSchedule(studentId));
    }

    @Operation(summary = "Enroll in a course section",
            description = "Validates prerequisites, time conflicts, grade level, max courses, classroom capacity")
    @ApiResponse(responseCode = "201", description = "Enrollment successful")
    @ApiResponse(responseCode = "404", description = "Student or section not found")
    @ApiResponse(responseCode = "409", description = "Enrollment validation failed")
    @PostMapping
    public ResponseEntity<EnrollmentDTO.EnrollmentResponse> enroll(
            @Valid @RequestBody EnrollmentDTO.EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enrollmentService.enroll(request.getStudentId(), request.getSectionId()));
    }

    @Operation(summary = "Drop a course section")
    @ApiResponse(responseCode = "204", description = "Successfully dropped")
    @ApiResponse(responseCode = "404", description = "Enrollment not found")
    @DeleteMapping("/{studentId}/{sectionId}")
    public ResponseEntity<Void> unenroll(
            @PathVariable Long studentId,
            @PathVariable Long sectionId) {
        enrollmentService.unenroll(studentId, sectionId);
        return ResponseEntity.noContent().build();
    }
}
package com.maplewood.controller;

import com.maplewood.dto.StudentDTO;
import com.maplewood.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Students", description = "Student profile and academic history")
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "Get all students")
    @ApiResponse(responseCode = "200", description = "List of students")
    @GetMapping
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @Operation(summary = "Get student profile", description = "Returns GPA, credits earned, course history")
    @ApiResponse(responseCode = "200", description = "Student profile found")
    @ApiResponse(responseCode = "404", description = "Student not found")
    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudentProfile(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentProfile(id));
    }
}
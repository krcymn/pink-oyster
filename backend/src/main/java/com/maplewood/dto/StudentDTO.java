package com.maplewood.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder(toBuilder = true)
public class StudentDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Integer gradeLevel;
    private Integer enrollmentYear;
    private Integer expectedGraduationYear;
    private String status;

    private Double gpa;
    private Double creditsEarned;
    private Double creditsRequired;
    private boolean graduationEligible;

    private List<CourseHistoryItemDTO> courseHistory;

    @Getter
    @Builder
    public static class CourseHistoryItemDTO {
        private Long courseId;
        private String courseCode;
        private String courseName;
        private Double credits;
        private String status; // passed / failed
        private String semesterName;
        private Integer semesterYear;
    }
}
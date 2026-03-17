package com.maplewood.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.List;

public class EnrollmentDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnrollmentRequest {

        @NotNull(message = "studentId is required")
        private Long studentId;

        @NotNull(message = "sectionId is required")
        private Long sectionId;
    }

    @Getter
    @Builder
    public static class EnrollmentResponse {
        private Long id;
        private Long studentId;
        private String studentFullName;
        private CourseSectionDTO courseSection;
    }

    @Getter
    @Builder
    public static class ScheduleResponse {
        private Long studentId;
        private String studentFullName;
        private Integer gradeLevel;
        private int totalEnrolled;
        private int maxAllowed;
        private List<CourseSectionDTO> sections;
    }
}
package com.maplewood.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CourseSectionDTO {

    private Long id;

    private Long courseId;
    private String courseCode;
    private String courseName;
    private String courseType;
    private BigDecimal credits;
    private Integer gradeLevelMin;
    private Integer gradeLevelMax;

    private Long prerequisiteId;
    private String prerequisiteCode;
    private String prerequisiteName;

    private Long semesterId;
    private String semesterName;
    private Integer semesterYear;

    private Long teacherId;
    private String teacherFullName;

    private Long classroomId;
    private String classroomName;
    private Integer classroomCapacity;

    private String daysOfWeek;   // ex: "MON,WED,FRI"
    private String startTime;    // ex: "09:00"
    private String endTime;      // ex: "10:00"
}
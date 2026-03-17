package com.maplewood.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CourseDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private BigDecimal credits;
    private Integer hoursPerWeek;
    private String courseType;
    private Integer gradeLevelMin;
    private Integer gradeLevelMax;
    private Integer semesterOrder;
    private String specializationName;

    private Long prerequisiteId;
    private String prerequisiteCode;
    private String prerequisiteName;
}
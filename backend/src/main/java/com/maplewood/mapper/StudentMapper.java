package com.maplewood.mapper;

import com.maplewood.dto.StudentDTO;
import com.maplewood.model.Student;
import com.maplewood.model.StudentCourseHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "gpa",                ignore = true)
    @Mapping(target = "creditsEarned",      ignore = true)
    @Mapping(target = "creditsRequired",    ignore = true)
    @Mapping(target = "graduationEligible", ignore = true)
    @Mapping(target = "courseHistory",      ignore = true)
    StudentDTO toSimpleDTO(Student student);

    @Mapping(target = "courseId",     source = "course.id")
    @Mapping(target = "courseCode",   source = "course.code")
    @Mapping(target = "courseName",   source = "course.name")
    @Mapping(target = "credits",      source = "course.credits")
    @Mapping(target = "semesterName", source = "semester.name")
    @Mapping(target = "semesterYear", source = "semester.year")
    StudentDTO.CourseHistoryItemDTO toHistoryItemDTO(StudentCourseHistory history);
}
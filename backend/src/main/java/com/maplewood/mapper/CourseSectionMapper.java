package com.maplewood.mapper;

import com.maplewood.dto.CourseSectionDTO;
import com.maplewood.model.CourseSection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseSectionMapper {

    @Mapping(target = "courseId",          source = "course.id")
    @Mapping(target = "courseCode",        source = "course.code")
    @Mapping(target = "courseName",        source = "course.name")
    @Mapping(target = "courseType",        source = "course.courseType")
    @Mapping(target = "credits",           source = "course.credits")
    @Mapping(target = "gradeLevelMin",     source = "course.gradeLevelMin")
    @Mapping(target = "gradeLevelMax",     source = "course.gradeLevelMax")
    @Mapping(target = "prerequisiteId",    source = "course.prerequisite.id")
    @Mapping(target = "prerequisiteCode",  source = "course.prerequisite.code")
    @Mapping(target = "prerequisiteName",  source = "course.prerequisite.name")
    @Mapping(target = "semesterId",        source = "semester.id")
    @Mapping(target = "semesterName",      source = "semester.name")
    @Mapping(target = "semesterYear",      source = "semester.year")
    @Mapping(target = "teacherId",         source = "teacher.id")
    @Mapping(target = "teacherFullName",   expression = "java(section.getTeacher().getFirstName() + \" \" + section.getTeacher().getLastName())")
    @Mapping(target = "classroomId",       source = "classroom.id")
    @Mapping(target = "classroomName",     source = "classroom.name")
    @Mapping(target = "classroomCapacity", source = "classroom.capacity")
    CourseSectionDTO toDTO(CourseSection section);
}
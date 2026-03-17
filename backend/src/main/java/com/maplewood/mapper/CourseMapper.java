package com.maplewood.mapper;

import com.maplewood.dto.CourseDTO;
import com.maplewood.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "specializationName", source = "specialization.name")
    @Mapping(target = "prerequisiteId", source = "prerequisite.id")
    @Mapping(target = "prerequisiteCode", source = "prerequisite.code")
    @Mapping(target = "prerequisiteName", source = "prerequisite.name")
    CourseDTO toDTO(Course course);
}
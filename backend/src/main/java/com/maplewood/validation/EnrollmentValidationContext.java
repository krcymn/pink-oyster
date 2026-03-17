package com.maplewood.validation;

import com.maplewood.model.CourseSection;
import com.maplewood.model.Semester;
import com.maplewood.model.Student;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EnrollmentValidationContext {
    private final Student student;
    private final CourseSection section;
    private final Semester activeSemester;
    private final List<CourseSection> enrolledSections;
    private final long currentEnrollmentCount;
    private final long enrolledInSection;
}
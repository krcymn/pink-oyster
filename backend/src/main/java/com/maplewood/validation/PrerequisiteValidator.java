package com.maplewood.validation;

import com.maplewood.exception.EnrollmentException;
import com.maplewood.model.Course;
import com.maplewood.repository.StudentCourseHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(6)
@RequiredArgsConstructor
public class PrerequisiteValidator implements EnrollmentValidator {

    private final StudentCourseHistoryRepository historyRepository;

    @Override
    public void validate(EnrollmentValidationContext ctx) {
        Course course = ctx.getSection().getCourse();
        if (course.getPrerequisite() == null) return;

        boolean hasPassed = historyRepository.hasStudentPassedCourse(
                ctx.getStudent().getId(), course.getPrerequisite().getId());

        if (!hasPassed) {
            throw EnrollmentException.prerequisiteNotMet(
                    course.getPrerequisite().getCode(),
                    course.getPrerequisite().getName(),
                    course.getPrerequisite().getId());
        }
    }
}
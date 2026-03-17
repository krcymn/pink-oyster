package com.maplewood.validation;

import com.maplewood.exception.EnrollmentException;
import com.maplewood.model.Course;
import com.maplewood.model.Student;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class GradeLevelValidator implements EnrollmentValidator {

    @Override
    public void validate(EnrollmentValidationContext ctx) {
        Student student = ctx.getStudent();
        Course course = ctx.getSection().getCourse();

        if (student.getGradeLevel() < course.getGradeLevelMin() ||
                student.getGradeLevel() > course.getGradeLevelMax()) {
            throw EnrollmentException.gradeLevelMismatch(
                    student.getGradeLevel(), course.getGradeLevelMin(), course.getGradeLevelMax());
        }
    }
}
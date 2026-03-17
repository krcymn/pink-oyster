package com.maplewood.validation;

import com.maplewood.exception.EnrollmentException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class MaxCoursesValidator implements EnrollmentValidator {

    private static final int MAX_COURSES = 5;

    @Override
    public void validate(EnrollmentValidationContext ctx) {
        if (ctx.getCurrentEnrollmentCount() >= MAX_COURSES) {
            throw EnrollmentException.maxCoursesReached(MAX_COURSES);
        }
    }
}
package com.maplewood.validation;

import com.maplewood.exception.EnrollmentException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
public class ClassroomCapacityValidator implements EnrollmentValidator {

    @Override
    public void validate(EnrollmentValidationContext ctx) {
        int capacity = ctx.getSection().getClassroom().getCapacity();
        if (ctx.getEnrolledInSection() >= capacity) {
            throw EnrollmentException.classroomFull(
                    ctx.getSection().getClassroom().getName(), capacity);
        }
    }
}
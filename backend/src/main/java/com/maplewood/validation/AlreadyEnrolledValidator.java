package com.maplewood.validation;

import com.maplewood.exception.EnrollmentException;
import com.maplewood.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
@RequiredArgsConstructor
public class AlreadyEnrolledValidator implements EnrollmentValidator {

    private final EnrollmentRepository enrollmentRepository;

    @Override
    public void validate(EnrollmentValidationContext ctx) {
        enrollmentRepository
                .findByStudentIdAndCourseSectionId(ctx.getStudent().getId(), ctx.getSection().getId())
                .ifPresent(e -> { throw EnrollmentException.alreadyEnrolled(); });
    }
}
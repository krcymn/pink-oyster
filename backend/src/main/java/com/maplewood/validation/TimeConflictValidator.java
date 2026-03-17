package com.maplewood.validation;

import com.maplewood.exception.EnrollmentException;
import com.maplewood.model.CourseSection;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(7)
public class TimeConflictValidator implements EnrollmentValidator {

    @Override
    public void validate(EnrollmentValidationContext ctx) {
        for (CourseSection existing : ctx.getEnrolledSections()) {
            if (hasConflict(existing, ctx.getSection())) {
                throw EnrollmentException.timeConflict(
                        existing.getCourse().getCode(),
                        existing.getDaysOfWeek(),
                        existing.getStartTime(),
                        existing.getEndTime());
            }
        }
    }

    private boolean hasConflict(CourseSection a, CourseSection b) {
        Set<String> daysA = Arrays.stream(a.getDaysOfWeek().split(",")).collect(Collectors.toSet());
        Set<String> daysB = Arrays.stream(b.getDaysOfWeek().split(",")).collect(Collectors.toSet());

        if (daysA.stream().noneMatch(daysB::contains)) return false;

        LocalTime startA = LocalTime.parse(a.getStartTime());
        LocalTime endA   = LocalTime.parse(a.getEndTime());
        LocalTime startB = LocalTime.parse(b.getStartTime());
        LocalTime endB   = LocalTime.parse(b.getEndTime());

        return startA.isBefore(endB) && startB.isBefore(endA);
    }
}
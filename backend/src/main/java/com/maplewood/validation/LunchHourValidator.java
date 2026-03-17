package com.maplewood.validation;

import com.maplewood.exception.EnrollmentException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@Order(2)
public class LunchHourValidator implements EnrollmentValidator {

    private static final LocalTime LUNCH_START = LocalTime.of(12, 0);
    private static final LocalTime LUNCH_END   = LocalTime.of(13, 0);

    @Override
    public void validate(EnrollmentValidationContext ctx) {
        LocalTime start = LocalTime.parse(ctx.getSection().getStartTime());
        LocalTime end   = LocalTime.parse(ctx.getSection().getEndTime());

        if (start.isBefore(LUNCH_END) && end.isAfter(LUNCH_START)) {
            throw EnrollmentException.lunchHour();
        }
    }
}
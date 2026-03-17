package com.maplewood.service;

import com.maplewood.model.StudentCourseHistory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GpaCalculator {

    private static final double CREDITS_REQUIRED = 30.0;
    private static final double GPA_SCALE = 4.0;

    public double calculateCreditsEarned(List<StudentCourseHistory> history) {
        return history.stream()
                .filter(h -> "passed".equals(h.getStatus()))
                .mapToDouble(h -> h.getCourse().getCredits().doubleValue())
                .sum();
    }

    public double calculateGpa(List<StudentCourseHistory> history) {
        double totalCredits = history.stream()
                .mapToDouble(h -> h.getCourse().getCredits().doubleValue())
                .sum();

        if (totalCredits == 0) return 0.0;

        double earnedCredits = calculateCreditsEarned(history);
        return Math.round((earnedCredits / totalCredits * GPA_SCALE) * 100.0) / 100.0;
    }

    public double getCreditsRequired() {
        return CREDITS_REQUIRED;
    }

    public boolean isGraduationEligible(double creditsEarned) {
        return creditsEarned >= CREDITS_REQUIRED;
    }
}
package com.maplewood.service;

import com.maplewood.model.Course;
import com.maplewood.model.StudentCourseHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GpaCalculatorTest {

    private GpaCalculator gpaCalculator;

    @BeforeEach
    void setUp() {
        gpaCalculator = new GpaCalculator();
    }

    private StudentCourseHistory history(String status, double credits) {
        Course course = new Course();
        course.setCredits(new BigDecimal(String.valueOf(credits)));

        StudentCourseHistory h = new StudentCourseHistory();
        h.setCourse(course);
        h.setStatus(status);
        return h;
    }

    @Test
    @DisplayName("All passed — credits earned equals total")
    void calculateCreditsEarned_allPassed() {
        List<StudentCourseHistory> history = List.of(
                history("passed", 1.0),
                history("passed", 0.5),
                history("passed", 1.0)
        );

        assertThat(gpaCalculator.calculateCreditsEarned(history)).isEqualTo(2.5);
    }

    @Test
    @DisplayName("Some failed — only passed credits count")
    void calculateCreditsEarned_someFailed() {
        List<StudentCourseHistory> history = List.of(
                history("passed", 1.0),
                history("failed", 1.0),
                history("passed", 0.5)
        );

        assertThat(gpaCalculator.calculateCreditsEarned(history)).isEqualTo(1.5);
    }

    @Test
    @DisplayName("All failed — credits earned is 0")
    void calculateCreditsEarned_allFailed() {
        List<StudentCourseHistory> history = List.of(
                history("failed", 1.0),
                history("failed", 0.5)
        );

        assertThat(gpaCalculator.calculateCreditsEarned(history)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Empty history — GPA is 0")
    void calculateGpa_emptyHistory_returnsZero() {
        assertThat(gpaCalculator.calculateGpa(List.of())).isEqualTo(0.0);
    }

    @Test
    @DisplayName("All passed — GPA is 4.0")
    void calculateGpa_allPassed_returnsFour() {
        List<StudentCourseHistory> history = List.of(
                history("passed", 1.0),
                history("passed", 1.0)
        );

        assertThat(gpaCalculator.calculateGpa(history)).isEqualTo(4.0);
    }

    @Test
    @DisplayName("Half passed — GPA is 2.0")
    void calculateGpa_halfPassed_returnsTwo() {
        List<StudentCourseHistory> history = List.of(
                history("passed", 1.0),
                history("failed", 1.0)
        );

        assertThat(gpaCalculator.calculateGpa(history)).isEqualTo(2.0);
    }

    @Test
    @DisplayName("Graduation eligible — 30 credits earned")
    void isGraduationEligible_thirtyCredits_returnsTrue() {
        assertThat(gpaCalculator.isGraduationEligible(30.0)).isTrue();
    }

    @Test
    @DisplayName("Not eligible — less than 30 credits")
    void isGraduationEligible_lessThanThirty_returnsFalse() {
        assertThat(gpaCalculator.isGraduationEligible(29.9)).isFalse();
    }

    @Test
    @DisplayName("Credits required is 30")
    void getCreditsRequired_returnsThirty() {
        assertThat(gpaCalculator.getCreditsRequired()).isEqualTo(30.0);
    }
}
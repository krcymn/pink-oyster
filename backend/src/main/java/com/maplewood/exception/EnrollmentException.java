package com.maplewood.exception;

import org.springframework.http.HttpStatus;

public class EnrollmentException extends BusinessException {

    public EnrollmentException(String errorCode, String message, String suggestedAction) {
        super(errorCode, message, suggestedAction, HttpStatus.CONFLICT);
    }

    public static EnrollmentException alreadyEnrolled() {
        return new EnrollmentException(
                "ALREADY_ENROLLED",
                "Student is already enrolled in this section",
                "Check your current schedule");
    }

    public static EnrollmentException maxCoursesReached(int max) {
        return new EnrollmentException(
                "MAX_COURSES_REACHED",
                "Maximum course limit reached (" + max + " courses per semester)",
                "Drop a course before adding a new one");
    }

    public static EnrollmentException gradeLevelMismatch(int studentGrade, int min, int max) {
        return new EnrollmentException(
                "GRADE_LEVEL_MISMATCH",
                "This course is for grade levels " + min + " to " + max
                        + ". Student is in grade " + studentGrade,
                "Browse courses available for grade " + studentGrade);
    }

    public static EnrollmentException prerequisiteNotMet(String code, String name, Long courseId) {
        return new EnrollmentException(
                "PREREQ_MISSING",
                "Prerequisite not met: must pass '" + code + " - " + name + "' first",
                "Enroll in course '" + code + "' (ID: " + courseId + ") first");
    }

    public static EnrollmentException timeConflict(String courseCode, String days, String start, String end) {
        return new EnrollmentException(
                "TIME_CONFLICT",
                "Time conflict with '" + courseCode + "' (" + days + " " + start + "-" + end + ")",
                "Choose a section with a different time slot");
    }

    public static EnrollmentException lunchHour() {
        return new EnrollmentException(
                "LUNCH_HOUR_CONFLICT",
                "Cannot schedule courses during lunch hour (12:00-13:00)",
                "Choose a section outside lunch hours");
    }

    public static EnrollmentException classroomFull(String roomName, int capacity) {
        return new EnrollmentException(
                "CLASSROOM_FULL",
                "Classroom is full: " + roomName + " (capacity: " + capacity + ")",
                "Choose a different section of this course");
    }
}
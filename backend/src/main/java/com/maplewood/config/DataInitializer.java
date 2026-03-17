package com.maplewood.config;

import com.maplewood.model.*;
import com.maplewood.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.Duration;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CourseSectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;

    private static final int MAX_TEACHER_DAILY_HOURS = 4;

    private static final Map<String, String> SPECIALIZATION_TO_ROOM_TYPE = Map.of(
            "Mathematics",        "classroom",
            "English",            "classroom",
            "Science",            "science_lab",
            "Social_Studies",     "classroom",
            "Arts",               "art_studio",
            "Music",              "music_room",
            "Physical_Education", "gym",
            "Computer_Science",   "computer_lab",
            "Foreign_Language",   "classroom"
    );

    private static final List<TimeSlot> TIME_SLOTS = List.of(
            new TimeSlot("MON,WED,FRI", LocalTime.of(8,  0), LocalTime.of(9,  0)),
            new TimeSlot("MON,WED,FRI", LocalTime.of(9,  0), LocalTime.of(10, 0)),
            new TimeSlot("MON,WED,FRI", LocalTime.of(10, 0), LocalTime.of(11, 0)),
            new TimeSlot("MON,WED,FRI", LocalTime.of(11, 0), LocalTime.of(12, 0)),
            new TimeSlot("MON,WED,FRI", LocalTime.of(13, 0), LocalTime.of(14, 0)),
            new TimeSlot("MON,WED,FRI", LocalTime.of(14, 0), LocalTime.of(15, 0)),
            new TimeSlot("TUE,THU",     LocalTime.of(8,  0), LocalTime.of(9,  30)),
            new TimeSlot("TUE,THU",     LocalTime.of(9,  30), LocalTime.of(11, 0)),
            new TimeSlot("TUE,THU",     LocalTime.of(11, 0), LocalTime.of(12, 0)),
            new TimeSlot("TUE,THU",     LocalTime.of(13, 0), LocalTime.of(14, 30)),
            new TimeSlot("TUE,THU",     LocalTime.of(14, 30), LocalTime.of(16, 0))
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (sectionRepository.count() > 0) {
            log.info("Course sections already initialized, skipping.");
            return;
        }

        Semester activeSemester = semesterRepository.findByIsActiveTrue()
                .orElseThrow(() -> new IllegalStateException("No active semester found"));

        List<Course> courses = courseRepository.findAll();
        List<Teacher> teachers = teacherRepository.findAll();
        List<Classroom> classrooms = classroomRepository.findAll();

        Map<String, Double> teacherDailyHours = new HashMap<>();

        int sectionCount = 0;
        int skippedCount = 0;
        int slotIndex = 0;

        for (Course course : courses) {
            String specializationName = course.getSpecialization().getName();
            String requiredRoomType = SPECIALIZATION_TO_ROOM_TYPE.getOrDefault(specializationName, "classroom");

            List<Teacher> eligibleTeachers = teachers.stream()
                    .filter(t -> t.getSpecialization().getName().equals(specializationName))
                    .toList();

            List<Classroom> eligibleClassrooms = classrooms.stream()
                    .filter(c -> c.getRoomType().getName().equals(requiredRoomType))
                    .toList();

            if (eligibleTeachers.isEmpty() || eligibleClassrooms.isEmpty()) {
                log.warn("No eligible teacher or classroom for course: {}", course.getCode());
                skippedCount++;
                continue;
            }

            boolean assigned = false;
            int attempts = 0;

            while (!assigned && attempts < TIME_SLOTS.size()) {
                TimeSlot slot = TIME_SLOTS.get(slotIndex % TIME_SLOTS.size());
                slotIndex++;
                attempts++;

                double slotHours = Duration.between(slot.start(), slot.end()).toMinutes() / 60.0;
                List<String> days = Arrays.asList(slot.days().split(","));

                Teacher assignedTeacher = null;
                for (Teacher teacher : eligibleTeachers) {
                    boolean teacherAvailable = true;
                    for (String day : days) {
                        String key = teacher.getId() + "_" + day;
                        double currentHours = teacherDailyHours.getOrDefault(key, 0.0);
                        if (currentHours + slotHours > MAX_TEACHER_DAILY_HOURS) {
                            teacherAvailable = false;
                            break;
                        }
                    }
                    if (teacherAvailable) {
                        assignedTeacher = teacher;
                        break;
                    }
                }

                if (assignedTeacher == null) continue;

                for (String day : days) {
                    String key = assignedTeacher.getId() + "_" + day;
                    teacherDailyHours.merge(key, slotHours, Double::sum);
                }

                Classroom classroom = eligibleClassrooms.get(sectionCount % eligibleClassrooms.size());

                CourseSection section = new CourseSection();
                section.setCourse(course);
                section.setSemester(activeSemester);
                section.setTeacher(assignedTeacher);
                section.setClassroom(classroom);
                section.setDaysOfWeek(slot.days());
                section.setStartTime(slot.start().toString());
                section.setEndTime(slot.end().toString());

                sectionRepository.save(section);
                sectionCount++;
                assigned = true;
            }

            if (!assigned) {
                log.warn("Could not assign section for course: {}", course.getCode());
                skippedCount++;
            }
        }

        log.info("DataInitializer: {} sections created, {} skipped — semester '{} {}'",
                sectionCount, skippedCount, activeSemester.getName(), activeSemester.getYear());
    }

    private record TimeSlot(String days, LocalTime start, LocalTime end) {}
}
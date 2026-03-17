package com.maplewood.service;

import com.maplewood.dto.CourseSectionDTO;
import com.maplewood.dto.EnrollmentDTO;
import com.maplewood.model.*;
import com.maplewood.repository.*;
import com.maplewood.validation.EnrollmentValidationContext;
import com.maplewood.validation.EnrollmentValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseSectionRepository sectionRepository;
    private final SemesterRepository semesterRepository;
    private final CourseSectionService sectionService;
    private final List<EnrollmentValidator> validators;

    @Transactional(readOnly = true)
    public EnrollmentDTO.ScheduleResponse getStudentSchedule(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + studentId));

        Semester activeSemester = semesterRepository.findByIsActiveTrue()
                .orElseThrow(() -> new EntityNotFoundException("No active semester found"));

        List<CourseSectionDTO> sections = enrollmentRepository
                .findByStudentAndSemester(studentId, activeSemester.getId())
                .stream()
                .map(e -> sectionService.getSectionById(e.getCourseSection().getId()))
                .toList();

        return EnrollmentDTO.ScheduleResponse.builder()
                .studentId(student.getId())
                .studentFullName(student.getFirstName() + " " + student.getLastName())
                .gradeLevel(student.getGradeLevel())
                .totalEnrolled(sections.size())
                .maxAllowed(5)
                .sections(sections)
                .build();
    }

    @Transactional
    public EnrollmentDTO.EnrollmentResponse enroll(Long studentId, Long sectionId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + studentId));

        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("Course section not found: " + sectionId));

        Semester activeSemester = semesterRepository.findByIsActiveTrue()
                .orElseThrow(() -> new EntityNotFoundException("No active semester found"));

        EnrollmentValidationContext context = EnrollmentValidationContext.builder()
                .student(student)
                .section(section)
                .activeSemester(activeSemester)
                .enrolledSections(sectionRepository.findEnrolledSectionsByStudentAndSemester(
                        studentId, activeSemester.getId()))
                .currentEnrollmentCount(enrollmentRepository.countByStudentAndSemester(
                        studentId, activeSemester.getId()))
                .enrolledInSection(enrollmentRepository.countByCourseSectionId(sectionId))
                .build();

        // Tüm validatörleri çalıştır
        validators.forEach(v -> v.validate(context));

        // Kayıt oluştur
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourseSection(section);
        Enrollment saved = enrollmentRepository.save(enrollment);

        return EnrollmentDTO.EnrollmentResponse.builder()
                .id(saved.getId())
                .studentId(student.getId())
                .studentFullName(student.getFirstName() + " " + student.getLastName())
                .courseSection(sectionService.getSectionById(sectionId))
                .build();
    }

    @Transactional
    public void unenroll(Long studentId, Long sectionId) {
        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndCourseSectionId(studentId, sectionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Enrollment not found for student " + studentId + " and section " + sectionId));
        enrollmentRepository.deleteById(enrollment.getId());
    }
}
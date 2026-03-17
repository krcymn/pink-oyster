package com.maplewood.service;

import com.maplewood.dto.StudentDTO;
import com.maplewood.mapper.StudentMapper;
import com.maplewood.model.StudentCourseHistory;
import com.maplewood.repository.StudentCourseHistoryRepository;
import com.maplewood.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentCourseHistoryRepository historyRepository;
    private final StudentMapper studentMapper;
    private final GpaCalculator gpaCalculator;

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(studentMapper::toSimpleDTO)
                .toList();
    }

    public StudentDTO getStudentProfile(Long studentId) {
        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));

        List<StudentCourseHistory> history = historyRepository.findByStudentId(studentId);
        double creditsEarned = gpaCalculator.calculateCreditsEarned(history);

        return studentMapper.toSimpleDTO(student).toBuilder()
                .gpa(gpaCalculator.calculateGpa(history))
                .creditsEarned(creditsEarned)
                .creditsRequired(gpaCalculator.getCreditsRequired())
                .graduationEligible(gpaCalculator.isGraduationEligible(creditsEarned))
                .courseHistory(history.stream().map(studentMapper::toHistoryItemDTO).toList())
                .build();
    }
}
package com.maplewood.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "students")
@Data
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String email;

    @Column(name = "grade_level", nullable = false)
    private Integer gradeLevel;

    @Column(name = "enrollment_year", nullable = false)
    private Integer enrollmentYear;

    @Column(name = "expected_graduation_year")
    private Integer expectedGraduationYear;

    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'active'")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "student")
    private List<StudentCourseHistory> courseHistory;
}

package com.maplewood.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Getter @Setter @NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal credits;

    @Column(name = "hours_per_week", nullable = false)
    private Integer hoursPerWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id", nullable = false)
    private Specialization specialization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_id")
    private Course prerequisite;

    @Column(name = "course_type", nullable = false)
    private String courseType; // "core" or "elective"

    @Column(name = "grade_level_min")
    private Integer gradeLevelMin;

    @Column(name = "grade_level_max")
    private Integer gradeLevelMax;

    @Column(name = "semester_order", nullable = false)
    private Integer semesterOrder; // 1=Fall, 2=Spring

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
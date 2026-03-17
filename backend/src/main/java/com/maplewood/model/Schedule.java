package com.maplewood.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;

@Entity
@Table(name = "schedules")
@Data
public class Schedule {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    private String dayOfWeek; // Monday, Wednesday vb.
    private LocalTime startTime;
    private LocalTime endTime;
    private Long semesterId;
}
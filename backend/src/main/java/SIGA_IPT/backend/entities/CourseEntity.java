package SIGA_IPT.backend.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "courses",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_code", "studyplan_id"})
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long course_id;

    @Column(name = "course_code", nullable = false)
    private String course_code;

    @Column(name = "course_name", nullable = false)
    private String course_name;

    @Column(name = "semester", nullable = false)
    private int semester;

    @Column(name = "theory_hours", nullable = false)
    private int theory_hours;

    @Column(name = "exercise_hours", nullable = false)
    private int exercise_hours;

    @Column(name = "lab_hours", nullable = false)
    private int lab_hours;

    @Column(name = "sct", nullable = false)
    private int sct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studyplan_id", nullable = false)
    private StudyplanEntity studyplan;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "course_prerequisites",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisite_course_id")
    )
    private List<CourseEntity> prerequisites = new ArrayList<>();

}
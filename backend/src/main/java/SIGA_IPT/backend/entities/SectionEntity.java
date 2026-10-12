package SIGA_IPT.backend.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "sections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "academic_period_id"})
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionEntity {

    public enum ScheduleDay {
        L,
        M,
        W,
        J,
        V
    }

    @Embeddable
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduleBlock {

        @Enumerated(EnumType.STRING)
        private ScheduleDay schedule_day;

        private int module;

        public String getBlockCode() {
            return schedule_day.name() + module;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long section_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_period_id", nullable = false)
    private AcademicPeriodEntity academic_period;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherEntity teacher;

    @Column(name = "section_capacity", nullable = false)
    private int section_capacity;

    @ElementCollection
    @CollectionTable(
            name = "section_schedule_blocks",
            joinColumns = @JoinColumn(name = "section_fk")
    )
    private List<ScheduleBlock> schedule_blocks = new ArrayList<>();

}
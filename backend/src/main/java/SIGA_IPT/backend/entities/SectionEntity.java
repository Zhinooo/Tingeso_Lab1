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
        private ScheduleDay scheduleDay;

        private int module;

        public String getBlockCode() {
            return scheduleDay.name() + module;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long sectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_period_id", nullable = false)
    private AcademicPeriodEntity academicPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherEntity teacher;

    @Column(name = "section_capacity", nullable = false)
    private int sectionCapacity;

    @ElementCollection
    @CollectionTable(
            name = "section_schedule_blocks",
            joinColumns = @JoinColumn(name = "section_fk")
    )
    private List<ScheduleBlock> scheduleBlocks = new ArrayList<>();

}
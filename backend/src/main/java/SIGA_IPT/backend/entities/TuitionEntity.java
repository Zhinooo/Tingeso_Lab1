package SIGA_IPT.backend.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(
        name = "tuitions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "academic_period_id"})
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TuitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long tuition_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_period_id", nullable = false)
    private AcademicPeriodEntity academic_period;

    @Column(name = "tuition_date", nullable = false)
    private LocalDate tuition_date;

}
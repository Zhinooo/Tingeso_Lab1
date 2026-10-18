package SIGA_IPT.backend.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
        name = "academic_periods",
        uniqueConstraints = @UniqueConstraint(columnNames = {"academic_year", "academic_semester"})
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcademicPeriodEntity {

    public enum AcademicPeriodStatus {
        OPEN,
        CLOSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long academicPeriodId;

    @Column(name = "academic_year", nullable = false)
    private int academicYear;

    @Column(name = "academic_semester", nullable = false)
    private int academicSemester;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_period_status", nullable = false)
    private AcademicPeriodStatus academicPeriodStatus;

}
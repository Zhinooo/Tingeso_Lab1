package SIGA_IPT.backend.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(
        name = "studyplans",
        uniqueConstraints = @UniqueConstraint(columnNames = {"studyplan_identifier", "career_id"})
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudyplanEntity {

    public enum StudyplanStatus{
        CURRENT,
        NOT_CURRENT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long studyplanId;

    @Column(name = "studyplan_identifier", nullable = false)
    private String studyplanIdentifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    private CareerEntity career;

    @Enumerated(EnumType.STRING)
    @Column(name = "studyplan_status", nullable = false)
    private StudyplanStatus studyplanStatus;

}
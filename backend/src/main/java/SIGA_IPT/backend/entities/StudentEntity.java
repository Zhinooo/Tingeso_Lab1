package SIGA_IPT.backend.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "students")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentEntity {

    public enum StudentStatus {
        REGULAR,
        DEFERRED,
        TEMPORARY_WITHDRAWAL,
        GRADUATED,
        EXPELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long student_id;

    @Column(name = "student_run", nullable = false, unique = true)
    private String student_run;

    @Column(name = "student_fullname", nullable = false)
    private String student_fullname;

    @Column(name = "student_email", nullable = false, unique = true)
    private String student_email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    private CareerEntity career;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studyplan_id", nullable = false)
    private StudyplanEntity studyplan;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_status", nullable = false)
    private StudentStatus student_status;

}
package SIGA_IPT.backend.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "students")
@Getter
@Setter
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
    private Long studentId;

    @Column(name = "student_run", nullable = false, unique = true)
    private String studentRun;

    @Column(name = "student_fullname", nullable = false)
    private String studentFullname;

    @Column(name = "student_email", nullable = false, unique = true)
    private String studentEmail;

    @Column(name = "keycloak_user_id", unique = true)
    private String keycloakUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    private CareerEntity career;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studyplan_id", nullable = false)
    private StudyplanEntity studyplan;

    @Enumerated(EnumType.STRING)
    @Column(name = "student_status", nullable = false)
    private StudentStatus studentStatus;

}
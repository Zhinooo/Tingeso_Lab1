package SIGA_IPT.backend.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "teachers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TeacherEntity {

    public enum TeacherStatus {
        ACTIVE,
        INACTIVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long teacherId;

    @Column(name = "teacher_run", nullable = false, unique = true)
    private String teacherRun;

    @Column(name = "teacher_fullname", nullable = false)
    private String teacherFullname;

    @Column(name = "teacher_email", nullable = false, unique = true)
    private String teacherEmail;

    @Column(name = "teacher_protitle", nullable = false)
    private String teacherProtitle;

    @Column(name = "teacher_degree", nullable = false)
    private String teacherDegree;

    @Enumerated(EnumType.STRING)
    @Column(name = "teacher_status", nullable = false)
    private TeacherStatus teacherStatus;

}
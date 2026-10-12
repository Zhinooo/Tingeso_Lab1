package SIGA_IPT.backend.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "teachers")
@Data
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
    private Long teacher_id;

    @Column(name = "teacher_run", nullable = false, unique = true)
    private String teacher_run;

    @Column(name = "teacher_fullname", nullable = false)
    private String teacher_fullname;

    @Column(name = "teacher_email", nullable = false, unique = true)
    private String teacher_email;

    @Column(name = "teacher_protitle", nullable = false)
    private String teacher_protitle;

    @Column(name = "teacher_degree", nullable = false)
    private String teacher_degree;

    @Enumerated(EnumType.STRING)
    @Column(name = "teacher_status", nullable = false)
    private TeacherStatus teacher_status;

}
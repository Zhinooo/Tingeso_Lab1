package SIGA_IPT.backend.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "careers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CareerEntity {

    public static final int DURATION_CAREER = 4;

    public enum CareerStatus {
        ACTIVE,
        INACTIVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long careerId;

    @Column(name = "career_code", nullable = false, unique = true)
    private String careerCode;

    @Column(name = "career_name", nullable = false)
    private String careerName;

    @Column(name = "career_description", nullable = false)
    private String careerDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "career_status", nullable = false)
    private CareerStatus careerStatus;

}
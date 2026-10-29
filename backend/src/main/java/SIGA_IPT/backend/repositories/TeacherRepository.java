package SIGA_IPT.backend.repositories;

import SIGA_IPT.backend.entities.TeacherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<TeacherEntity, Long>{

    boolean existsByTeacherRun(String teacherRun);

    boolean existsByTeacherEmail(String teacherEmail);

    Optional<TeacherEntity> findByTeacherEmail(String teacherEmail);

    Optional<TeacherEntity> findByKeycloakUserId(String keycloakUserId);

}

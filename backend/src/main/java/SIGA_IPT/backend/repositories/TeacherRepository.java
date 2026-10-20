package SIGA_IPT.backend.repositories;

import SIGA_IPT.backend.entities.TeacherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<TeacherEntity, Long>{

    boolean existsByTeacherRun(String teacherRun);

    boolean existsByTeacherEmail(String teacherEmail);

}

package SIGA_IPT.backend.repositories;

import SIGA_IPT.backend.entities.CareerEntity;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, Long>{

    boolean existsByCareer(CareerEntity career);

    boolean existsByStudyplan(StudyplanEntity studyplan);

}

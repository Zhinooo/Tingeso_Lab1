package SIGA_IPT.backend.repositories;

import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.TuitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TuitionRepository extends JpaRepository<TuitionEntity, Long>{

    boolean existsByAcademicPeriod(AcademicPeriodEntity academicPeriod);

}

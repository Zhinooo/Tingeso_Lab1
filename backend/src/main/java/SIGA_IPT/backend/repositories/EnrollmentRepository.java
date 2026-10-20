package SIGA_IPT.backend.repositories;

import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.EnrollmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long>{

    boolean existsByAcademicPeriod(AcademicPeriodEntity academicPeriod);

    boolean existsByCourse(CourseEntity course);

}

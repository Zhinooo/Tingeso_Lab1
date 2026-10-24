package SIGA_IPT.backend.repositories;

import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.entities.TuitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TuitionRepository extends JpaRepository<TuitionEntity, Long>{

    boolean existsByAcademicPeriod(AcademicPeriodEntity academicPeriod);

    boolean existsByStudent(StudentEntity student);

    boolean existsByStudentAndAcademicPeriod(StudentEntity student, AcademicPeriodEntity academicPeriod);

    List<TuitionEntity> findByStudent(StudentEntity student);

    List<TuitionEntity> findByAcademicPeriod(AcademicPeriodEntity academicPeriod);

}

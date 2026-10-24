package SIGA_IPT.backend.repositories;

import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.EnrollmentEntity;
import SIGA_IPT.backend.entities.SectionEntity;
import SIGA_IPT.backend.entities.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long>{

    boolean existsByAcademicPeriod(AcademicPeriodEntity academicPeriod);

    boolean existsByCourse(CourseEntity course);

    boolean existsByStudent(StudentEntity student);

    boolean existsByStudentAndAcademicPeriod(StudentEntity student, AcademicPeriodEntity academicPeriod);

    boolean existsBySection(SectionEntity section);

    long countBySection(SectionEntity section);

    List<EnrollmentEntity> findByStudent(StudentEntity student);

    List<EnrollmentEntity> findByAcademicPeriod(AcademicPeriodEntity academicPeriod);

    List<EnrollmentEntity> findBySection(SectionEntity section);

    List<EnrollmentEntity> findByStudentAndAcademicPeriod(StudentEntity student, AcademicPeriodEntity academicPeriod);

    List<EnrollmentEntity> findByStudentAndAcademicPeriod_AcademicPeriodStatus(StudentEntity student, AcademicPeriodEntity.AcademicPeriodStatus status);

}

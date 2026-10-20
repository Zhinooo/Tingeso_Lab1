package SIGA_IPT.backend.repositories;

import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.SectionEntity;
import SIGA_IPT.backend.entities.TeacherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionRepository extends JpaRepository<SectionEntity, Long>{

    boolean existsByTeacher(TeacherEntity teacher);

    boolean existsByTeacherAndAcademicPeriod_AcademicPeriodStatus(TeacherEntity teacher, AcademicPeriodEntity.AcademicPeriodStatus status);

    boolean existsByAcademicPeriod(AcademicPeriodEntity academicPeriod);

    boolean existsByCourse(CourseEntity course);

}

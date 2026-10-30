package SIGA_IPT.backend.repositories;

import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.SectionEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import SIGA_IPT.backend.entities.TeacherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<SectionEntity, Long>{

    boolean existsByTeacher(TeacherEntity teacher);

    boolean existsByTeacherAndAcademicPeriod_AcademicPeriodStatus(TeacherEntity teacher, AcademicPeriodEntity.AcademicPeriodStatus status);

    boolean existsByAcademicPeriod(AcademicPeriodEntity academicPeriod);

    boolean existsByCourse(CourseEntity course);

    boolean existsByCourseAndAcademicPeriod(CourseEntity course, AcademicPeriodEntity academicPeriod);

    List<SectionEntity> findByTeacherAndAcademicPeriod(TeacherEntity teacher, AcademicPeriodEntity academicPeriod);

    List<SectionEntity> findByAcademicPeriod(AcademicPeriodEntity academicPeriod);

    List<SectionEntity> findByAcademicPeriodAndCourse_StudyplanAndCourse_Semester(AcademicPeriodEntity academicPeriod, StudyplanEntity studyplan, int semester);

    List<SectionEntity> findByTeacher(TeacherEntity teacher);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SectionEntity> findForUpdateBySectionId(Long sectionId);

}

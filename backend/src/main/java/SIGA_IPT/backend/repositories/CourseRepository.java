package SIGA_IPT.backend.repositories;

import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long>{

    boolean existsByStudyplan(StudyplanEntity studyplan);

    boolean existsByCourseCodeAndStudyplan(String courseCode, StudyplanEntity studyplan);

    Optional<CourseEntity> findByCourseCodeAndStudyplan(String courseCode, StudyplanEntity studyplan);

    List<CourseEntity> findByStudyplan(StudyplanEntity studyplan);

    @Query("select count(c) > 0 from CourseEntity c join c.prerequisites p where p = :course")
    boolean isPrerequisiteOfAnyCourse(@Param("course") CourseEntity course);

}

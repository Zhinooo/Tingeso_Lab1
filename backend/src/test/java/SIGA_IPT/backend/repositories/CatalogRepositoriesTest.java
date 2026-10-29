package SIGA_IPT.backend.repositories;

import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.CareerEntity;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import SIGA_IPT.backend.entities.TeacherEntity;
import SIGA_IPT.backend.support.DataFixtures;
import SIGA_IPT.backend.support.MySqlContainerBase;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CatalogRepositoriesTest extends MySqlContainerBase {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CareerRepository careerRepository;
    @Autowired
    private StudyplanRepository studyplanRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private AcademicPeriodRepository academicPeriodRepository;

    private DataFixtures data;

    @BeforeEach
    void setUp(){
        data = new DataFixtures(entityManager);
    }

    @Test
    void careerIsFoundByItsCode(){
        CareerEntity career = data.career("C-100");

        assertThat(careerRepository.existsByCareerCode("C-100")).isTrue();
        assertThat(careerRepository.existsByCareerCode("NOPE")).isFalse();
        assertThat(careerRepository.findByCareerCode("C-100")).get().isEqualTo(career);
        assertThat(careerRepository.findByCareerCode("NOPE")).isEmpty();
    }

    @Test
    void careerCodesAreUnique(){
        data.career("C-200");

        assertThatThrownBy(() -> data.career("C-200")).isInstanceOf(Exception.class);
    }

    @Test
    void studyplanQueriesAreScopedByCareer(){
        CareerEntity career = data.career("C-300");
        CareerEntity other = data.career("C-301");
        StudyplanEntity current = data.studyplan(career, "2021.3", StudyplanEntity.StudyplanStatus.CURRENT);
        data.studyplan(career, "2019.1", StudyplanEntity.StudyplanStatus.NOT_CURRENT);

        assertThat(studyplanRepository.existsByCareer(career)).isTrue();
        assertThat(studyplanRepository.existsByCareer(other)).isFalse();
        assertThat(studyplanRepository.existsByStudyplanIdentifierAndCareer("2021.3", career)).isTrue();
        assertThat(studyplanRepository.existsByStudyplanIdentifierAndCareer("2021.3", other)).isFalse();
        assertThat(studyplanRepository.findByCareerAndStudyplanStatus(career, StudyplanEntity.StudyplanStatus.CURRENT))
                .get().isEqualTo(current);
        assertThat(studyplanRepository.findByStudyplanIdentifierAndCareer("2019.1", career)).isPresent();
        assertThat(studyplanRepository.findByStudyplanIdentifierAndCareer("2019.1", other)).isEmpty();
    }

    @Test
    void studyplanIdentifierIsUniquePerCareerButNotGlobally(){
        CareerEntity career = data.career("C-310");
        CareerEntity other = data.career("C-311");
        data.studyplan(career, "2021.3", StudyplanEntity.StudyplanStatus.CURRENT);

        assertThat(data.studyplan(other, "2021.3", StudyplanEntity.StudyplanStatus.CURRENT)).isNotNull();
        assertThatThrownBy(() -> data.studyplan(career, "2021.3", StudyplanEntity.StudyplanStatus.NOT_CURRENT))
                .isInstanceOf(Exception.class);
    }

    @Test
    void courseQueriesAreScopedByStudyplan(){
        CareerEntity career = data.career("C-400");
        StudyplanEntity plan = data.studyplan(career, "P1", StudyplanEntity.StudyplanStatus.CURRENT);
        StudyplanEntity otherPlan = data.studyplan(career, "P2", StudyplanEntity.StudyplanStatus.NOT_CURRENT);
        CourseEntity first = data.course(plan, "TPI101", 1);
        data.course(plan, "TPI102", 1);

        assertThat(courseRepository.existsByStudyplan(plan)).isTrue();
        assertThat(courseRepository.existsByStudyplan(otherPlan)).isFalse();
        assertThat(courseRepository.existsByCourseCodeAndStudyplan("TPI101", plan)).isTrue();
        assertThat(courseRepository.existsByCourseCodeAndStudyplan("TPI101", otherPlan)).isFalse();
        assertThat(courseRepository.findByCourseCodeAndStudyplan("TPI101", plan)).get().isEqualTo(first);
        assertThat(courseRepository.findByStudyplan(plan)).hasSize(2);
        assertThat(courseRepository.findByStudyplan(otherPlan)).isEmpty();
    }

    @Test
    void sameCourseCodeCanExistInDifferentPlansButNotTwiceInTheSameOne(){
        CareerEntity career = data.career("C-410");
        StudyplanEntity plan = data.studyplan(career, "P1", StudyplanEntity.StudyplanStatus.CURRENT);
        StudyplanEntity otherPlan = data.studyplan(career, "P2", StudyplanEntity.StudyplanStatus.NOT_CURRENT);
        data.course(plan, "TPI101", 1);

        assertThat(data.course(otherPlan, "TPI101", 1)).isNotNull();
        assertThatThrownBy(() -> data.course(plan, "TPI101", 2)).isInstanceOf(Exception.class);
    }

    @Test
    void prerequisiteQueryDetectsCoursesUsedAsPrerequisite(){
        CareerEntity career = data.career("C-420");
        StudyplanEntity plan = data.studyplan(career, "P1", StudyplanEntity.StudyplanStatus.CURRENT);
        CourseEntity base = data.course(plan, "TPI101", 1);
        CourseEntity advanced = data.course(plan, "TPI201", 2, base);
        entityManager.clear();

        assertThat(courseRepository.isPrerequisiteOfAnyCourse(courseRepository.findById(base.getCourseId()).orElseThrow())).isTrue();
        assertThat(courseRepository.isPrerequisiteOfAnyCourse(courseRepository.findById(advanced.getCourseId()).orElseThrow())).isFalse();
    }

    @Test
    void prerequisitesArePersistedAsAList(){
        CareerEntity career = data.career("C-430");
        StudyplanEntity plan = data.studyplan(career, "P1", StudyplanEntity.StudyplanStatus.CURRENT);
        CourseEntity a = data.course(plan, "A", 1);
        CourseEntity b = data.course(plan, "B", 1);
        CourseEntity c = data.course(plan, "C", 2, a, b);
        entityManager.clear();

        CourseEntity loaded = courseRepository.findById(c.getCourseId()).orElseThrow();

        assertThat(loaded.getPrerequisites()).extracting(CourseEntity::getCourseCode).containsExactlyInAnyOrder("A", "B");
    }

    @Test
    void studentQueriesFindByCareerPlanRunAndEmail(){
        CareerEntity career = data.career("C-500");
        CareerEntity other = data.career("C-501");
        StudyplanEntity plan = data.studyplan(career, "P1", StudyplanEntity.StudyplanStatus.CURRENT);
        StudyplanEntity otherPlan = data.studyplan(other, "P1", StudyplanEntity.StudyplanStatus.CURRENT);
        StudentEntity student = data.student(career, plan);

        assertThat(studentRepository.existsByCareer(career)).isTrue();
        assertThat(studentRepository.existsByCareer(other)).isFalse();
        assertThat(studentRepository.existsByStudyplan(plan)).isTrue();
        assertThat(studentRepository.existsByStudyplan(otherPlan)).isFalse();
        assertThat(studentRepository.existsByStudentRun(student.getStudentRun())).isTrue();
        assertThat(studentRepository.existsByStudentRun("none")).isFalse();
        assertThat(studentRepository.existsByStudentEmail(student.getStudentEmail())).isTrue();
        assertThat(studentRepository.findByStudentEmail(student.getStudentEmail())).get().isEqualTo(student);
        assertThat(studentRepository.findByStudentEmail("none@test.cl")).isEmpty();
        assertThat(studentRepository.findByKeycloakUserId(student.getKeycloakUserId())).get().isEqualTo(student);
        assertThat(studentRepository.findByKeycloakUserId("none")).isEmpty();
    }

    @Test
    void studentRunAndEmailAreUnique(){
        CareerEntity career = data.career("C-510");
        StudyplanEntity plan = data.studyplan(career, "P1", StudyplanEntity.StudyplanStatus.CURRENT);
        StudentEntity student = data.student(career, plan);
        StudentEntity duplicate = new StudentEntity();
        duplicate.setStudentRun(student.getStudentRun());
        duplicate.setStudentFullname("Other");
        duplicate.setStudentEmail("other@test.cl");
        duplicate.setCareer(career);
        duplicate.setStudyplan(plan);
        duplicate.setStudentStatus(StudentEntity.StudentStatus.REGULAR);

        assertThatThrownBy(() -> {
            studentRepository.saveAndFlush(duplicate);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void teacherQueriesFindByRunAndEmail(){
        TeacherEntity teacher = data.teacher();

        assertThat(teacherRepository.existsByTeacherRun(teacher.getTeacherRun())).isTrue();
        assertThat(teacherRepository.existsByTeacherRun("none")).isFalse();
        assertThat(teacherRepository.existsByTeacherEmail(teacher.getTeacherEmail())).isTrue();
        assertThat(teacherRepository.existsByTeacherEmail("none@test.cl")).isFalse();
        assertThat(teacherRepository.findByTeacherEmail(teacher.getTeacherEmail())).get().isEqualTo(teacher);
        assertThat(teacherRepository.findByTeacherEmail("none@test.cl")).isEmpty();
        assertThat(teacherRepository.findByKeycloakUserId(teacher.getKeycloakUserId())).get().isEqualTo(teacher);
        assertThat(teacherRepository.findByKeycloakUserId("none")).isEmpty();
    }

    @Test
    void theKeycloakUserIdIsUnique(){
        TeacherEntity teacher = data.teacher();
        TeacherEntity duplicate = new TeacherEntity();
        duplicate.setTeacherRun("T-dup");
        duplicate.setTeacherFullname("Other");
        duplicate.setTeacherEmail("other.teacher@test.cl");
        duplicate.setTeacherProtitle("Engineer");
        duplicate.setTeacherDegree("Master");
        duplicate.setTeacherStatus(TeacherEntity.TeacherStatus.ACTIVE);
        duplicate.setKeycloakUserId(teacher.getKeycloakUserId());

        assertThatThrownBy(() -> {
            teacherRepository.saveAndFlush(duplicate);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void academicPeriodsAreUniquePerYearAndSemester(){
        data.period(2900, 1, AcademicPeriodEntity.AcademicPeriodStatus.OPEN);

        assertThat(academicPeriodRepository.existsByAcademicYearAndAcademicSemester(2900, 1)).isTrue();
        assertThat(academicPeriodRepository.existsByAcademicYearAndAcademicSemester(2900, 2)).isFalse();
        assertThat(data.period(2900, 2, AcademicPeriodEntity.AcademicPeriodStatus.OPEN)).isNotNull();
        assertThatThrownBy(() -> data.period(2900, 1, AcademicPeriodEntity.AcademicPeriodStatus.CLOSED))
                .isInstanceOf(Exception.class);
    }

    @Test
    void savingAndReadingBackKeepsTheEnumStatuses(){
        CareerEntity career = data.career("C-600");
        entityManager.clear();

        List<CareerEntity> careers = careerRepository.findAll();

        assertThat(careers).anyMatch(c -> c.getCareerCode().equals("C-600")
                && c.getCareerStatus() == CareerEntity.CareerStatus.ACTIVE);
        assertThat(career.getCareerId()).isNotNull();
    }
}

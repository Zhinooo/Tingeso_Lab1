package SIGA_IPT.backend.repositories;

import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.CareerEntity;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.EnrollmentEntity;
import SIGA_IPT.backend.entities.SectionEntity;
import SIGA_IPT.backend.entities.SectionEntity.ScheduleDay;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import SIGA_IPT.backend.entities.TeacherEntity;
import SIGA_IPT.backend.entities.TuitionEntity;
import SIGA_IPT.backend.support.DataFixtures;
import SIGA_IPT.backend.support.MySqlContainerBase;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AcademicRepositoriesTest extends MySqlContainerBase {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private TuitionRepository tuitionRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private DataFixtures data;
    private CareerEntity career;
    private StudyplanEntity plan;
    private CourseEntity course;
    private AcademicPeriodEntity openPeriod;
    private AcademicPeriodEntity closedPeriod;
    private TeacherEntity teacher;
    private StudentEntity student;

    @BeforeEach
    void setUp(){
        data = new DataFixtures(entityManager);
        career = data.career("R-" + System.nanoTime());
        plan = data.studyplan(career, "P1", StudyplanEntity.StudyplanStatus.CURRENT);
        course = data.course(plan, "TPI101", 1);
        openPeriod = data.period(2800 + (int) (System.nanoTime() % 100000), 1, AcademicPeriodEntity.AcademicPeriodStatus.OPEN);
        closedPeriod = data.period(openPeriod.getAcademicYear(), 2, AcademicPeriodEntity.AcademicPeriodStatus.CLOSED);
        teacher = data.teacher();
        student = data.student(career, plan);
    }

    @Test
    void sectionQueriesFilterByTeacherPeriodAndCourse(){
        SectionEntity section = data.section(course, openPeriod, teacher, 30, ScheduleDay.L, 1);
        TeacherEntity other = data.teacher();

        assertThat(sectionRepository.existsByTeacher(teacher)).isTrue();
        assertThat(sectionRepository.existsByTeacher(other)).isFalse();
        assertThat(sectionRepository.existsByAcademicPeriod(openPeriod)).isTrue();
        assertThat(sectionRepository.existsByAcademicPeriod(closedPeriod)).isFalse();
        assertThat(sectionRepository.existsByCourse(course)).isTrue();
        assertThat(sectionRepository.existsByCourseAndAcademicPeriod(course, openPeriod)).isTrue();
        assertThat(sectionRepository.existsByCourseAndAcademicPeriod(course, closedPeriod)).isFalse();
        assertThat(sectionRepository.findByTeacher(teacher)).containsExactly(section);
        assertThat(sectionRepository.findByAcademicPeriod(openPeriod)).containsExactly(section);
        assertThat(sectionRepository.findByTeacherAndAcademicPeriod(teacher, openPeriod)).containsExactly(section);
        assertThat(sectionRepository.findByTeacherAndAcademicPeriod(teacher, closedPeriod)).isEmpty();
    }

    @Test
    void sectionQueryByTeacherAndPeriodStatusOnlyMatchesOpenPeriods(){
        data.section(course, openPeriod, teacher, 30, ScheduleDay.L, 1);
        TeacherEntity closedTeacher = data.teacher();
        CourseEntity otherCourse = data.course(plan, "TPI102", 1);
        data.section(otherCourse, closedPeriod, closedTeacher, 30, ScheduleDay.M, 1);

        assertThat(sectionRepository.existsByTeacherAndAcademicPeriod_AcademicPeriodStatus(
                teacher, AcademicPeriodEntity.AcademicPeriodStatus.OPEN)).isTrue();
        assertThat(sectionRepository.existsByTeacherAndAcademicPeriod_AcademicPeriodStatus(
                closedTeacher, AcademicPeriodEntity.AcademicPeriodStatus.OPEN)).isFalse();
    }

    @Test
    void aCourseCanHaveOnlyOneSectionPerPeriod(){
        data.section(course, openPeriod, teacher, 30, ScheduleDay.L, 1);

        assertThatThrownBy(() -> data.section(course, openPeriod, data.teacher(), 20, ScheduleDay.M, 2))
                .isInstanceOf(Exception.class);
    }

    @Test
    void scheduleBlocksAreStoredWithTheSection(){
        SectionEntity section = data.section(course, openPeriod, teacher, 30, ScheduleDay.J, 3);
        entityManager.clear();

        SectionEntity loaded = sectionRepository.findById(section.getSectionId()).orElseThrow();

        assertThat(loaded.getScheduleBlocks()).extracting(SectionEntity.ScheduleBlock::getBlockCode).containsExactly("J3");
    }

    @Test
    void findForUpdateLocksAndReturnsTheSection(){
        SectionEntity section = data.section(course, openPeriod, teacher, 30, ScheduleDay.L, 1);

        assertThat(sectionRepository.findForUpdateBySectionId(section.getSectionId())).get().isEqualTo(section);
        assertThat(sectionRepository.findForUpdateBySectionId(-1L)).isEmpty();
    }

    @Test
    void tuitionQueriesFilterByStudentAndPeriod(){
        TuitionEntity tuition = data.tuition(student, openPeriod);
        StudentEntity other = data.student(career, plan);

        assertThat(tuitionRepository.existsByStudent(student)).isTrue();
        assertThat(tuitionRepository.existsByStudent(other)).isFalse();
        assertThat(tuitionRepository.existsByAcademicPeriod(openPeriod)).isTrue();
        assertThat(tuitionRepository.existsByAcademicPeriod(closedPeriod)).isFalse();
        assertThat(tuitionRepository.existsByStudentAndAcademicPeriod(student, openPeriod)).isTrue();
        assertThat(tuitionRepository.existsByStudentAndAcademicPeriod(student, closedPeriod)).isFalse();
        assertThat(tuitionRepository.findByStudent(student)).containsExactly(tuition);
        assertThat(tuitionRepository.findByAcademicPeriod(openPeriod)).containsExactly(tuition);
    }

    @Test
    void aStudentHasOneTuitionPerPeriod(){
        data.tuition(student, openPeriod);

        assertThatThrownBy(() -> data.tuition(student, openPeriod)).isInstanceOf(Exception.class);
    }

    @Test
    void enrollmentQueriesFilterByStudentSectionAndPeriod(){
        SectionEntity section = data.section(course, openPeriod, teacher, 30, ScheduleDay.L, 1);
        EnrollmentEntity enrollment = data.enrollment(student, section, null);
        StudentEntity other = data.student(career, plan);

        assertThat(enrollmentRepository.existsByStudent(student)).isTrue();
        assertThat(enrollmentRepository.existsByStudent(other)).isFalse();
        assertThat(enrollmentRepository.existsByCourse(course)).isTrue();
        assertThat(enrollmentRepository.existsBySection(section)).isTrue();
        assertThat(enrollmentRepository.existsByAcademicPeriod(openPeriod)).isTrue();
        assertThat(enrollmentRepository.existsByAcademicPeriod(closedPeriod)).isFalse();
        assertThat(enrollmentRepository.existsByStudentAndAcademicPeriod(student, openPeriod)).isTrue();
        assertThat(enrollmentRepository.existsByStudentAndAcademicPeriod(other, openPeriod)).isFalse();
        assertThat(enrollmentRepository.countBySection(section)).isEqualTo(1);
        assertThat(enrollmentRepository.findByStudent(student)).containsExactly(enrollment);
        assertThat(enrollmentRepository.findBySection(section)).containsExactly(enrollment);
        assertThat(enrollmentRepository.findByAcademicPeriod(openPeriod)).containsExactly(enrollment);
        assertThat(enrollmentRepository.findByStudentAndAcademicPeriod(student, openPeriod)).containsExactly(enrollment);
        assertThat(enrollmentRepository.findByStudentAndAcademicPeriod(student, closedPeriod)).isEmpty();
    }

    @Test
    void historyQueryOnlyReturnsEnrollmentsOfClosedPeriods(){
        CourseEntity otherCourse = data.course(plan, "TPI102", 1);
        SectionEntity openSection = data.section(course, openPeriod, teacher, 30, ScheduleDay.L, 1);
        SectionEntity closedSection = data.section(otherCourse, closedPeriod, data.teacher(), 30, ScheduleDay.M, 1);
        data.enrollment(student, openSection, null);
        EnrollmentEntity past = data.enrollment(student, closedSection, 5.5);

        assertThat(enrollmentRepository.findByStudentAndAcademicPeriod_AcademicPeriodStatus(
                student, AcademicPeriodEntity.AcademicPeriodStatus.CLOSED)).containsExactly(past);
        assertThat(enrollmentRepository.findByStudentAndAcademicPeriod_AcademicPeriodStatus(
                student, AcademicPeriodEntity.AcademicPeriodStatus.OPEN)).hasSize(1);
    }

    @Test
    void countBySectionCountsEveryEnrolledStudent(){
        SectionEntity section = data.section(course, openPeriod, teacher, 30, ScheduleDay.L, 1);
        data.enrollment(student, section, null);
        data.enrollment(data.student(career, plan), section, null);

        assertThat(enrollmentRepository.countBySection(section)).isEqualTo(2);
    }

    @Test
    void aStudentCannotBeEnrolledTwiceInTheSameSection(){
        SectionEntity section = data.section(course, openPeriod, teacher, 30, ScheduleDay.L, 1);
        data.enrollment(student, section, null);

        assertThatThrownBy(() -> data.enrollment(student, section, null)).isInstanceOf(Exception.class);
    }

    @Test
    void finalGradesAreStoredAsDecimals(){
        SectionEntity section = data.section(course, openPeriod, teacher, 30, ScheduleDay.L, 1);
        EnrollmentEntity enrollment = data.enrollment(student, section, 5.7);
        entityManager.clear();

        assertThat(enrollmentRepository.findById(enrollment.getEnrollmentId()).orElseThrow().getFinalGrade()).isEqualTo(5.7);
        assertThat(student.getStudentStatus()).isEqualTo(StudentEntity.StudentStatus.REGULAR);
    }
}

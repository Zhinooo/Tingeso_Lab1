package SIGA_IPT.backend.support;

import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.CareerEntity;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.EnrollmentEntity;
import SIGA_IPT.backend.entities.SectionEntity;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import SIGA_IPT.backend.entities.TeacherEntity;
import SIGA_IPT.backend.entities.TuitionEntity;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DataFixtures {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private final EntityManager entityManager;

    public DataFixtures(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    private <T> T save(T entity){
        entityManager.persist(entity);
        entityManager.flush();
        return entity;
    }

    public CareerEntity career(String code){
        CareerEntity career = new CareerEntity();
        career.setCareerCode(code);
        career.setCareerName("Career " + code);
        career.setCareerDescription("Description");
        career.setCareerStatus(CareerEntity.CareerStatus.ACTIVE);
        return save(career);
    }

    public StudyplanEntity studyplan(CareerEntity career, String identifier, StudyplanEntity.StudyplanStatus status){
        StudyplanEntity plan = new StudyplanEntity();
        plan.setStudyplanIdentifier(identifier);
        plan.setCareer(career);
        plan.setStudyplanStatus(status);
        return save(plan);
    }

    public CourseEntity course(StudyplanEntity plan, String code, int semester, CourseEntity... prerequisites){
        CourseEntity course = new CourseEntity();
        course.setCourseCode(code);
        course.setCourseName("Course " + code);
        course.setSemester(semester);
        course.setTheoryHours(2);
        course.setExerciseHours(2);
        course.setLabHours(0);
        course.setSct(5);
        course.setStudyplan(plan);
        course.setPrerequisites(new ArrayList<>(List.of(prerequisites)));
        return save(course);
    }

    public AcademicPeriodEntity period(int year, int semester, AcademicPeriodEntity.AcademicPeriodStatus status){
        AcademicPeriodEntity period = new AcademicPeriodEntity();
        period.setAcademicYear(year);
        period.setAcademicSemester(semester);
        period.setAcademicPeriodStatus(status);
        return save(period);
    }

    public TeacherEntity teacher(){
        int n = SEQUENCE.incrementAndGet();
        TeacherEntity teacher = new TeacherEntity();
        teacher.setTeacherRun("T-" + n);
        teacher.setTeacherFullname("Teacher " + n);
        teacher.setTeacherEmail("teacher" + n + "@test.cl");
        teacher.setKeycloakUserId("kc-teacher-" + n);
        teacher.setTeacherProtitle("Engineer");
        teacher.setTeacherDegree("Master");
        teacher.setTeacherStatus(TeacherEntity.TeacherStatus.ACTIVE);
        return save(teacher);
    }

    public StudentEntity student(CareerEntity career, StudyplanEntity plan){
        int n = SEQUENCE.incrementAndGet();
        StudentEntity student = new StudentEntity();
        student.setStudentRun("S-" + n);
        student.setStudentFullname("Student " + n);
        student.setStudentEmail("student" + n + "@test.cl");
        student.setKeycloakUserId("kc-student-" + n);
        student.setCareer(career);
        student.setStudyplan(plan);
        student.setStudentStatus(StudentEntity.StudentStatus.REGULAR);
        return save(student);
    }

    public SectionEntity section(CourseEntity course, AcademicPeriodEntity period, TeacherEntity teacher, int capacity,
                                 SectionEntity.ScheduleDay day, int module){
        SectionEntity section = new SectionEntity();
        section.setCourse(course);
        section.setAcademicPeriod(period);
        section.setTeacher(teacher);
        section.setSectionCapacity(capacity);
        section.setScheduleBlocks(new ArrayList<>(List.of(new SectionEntity.ScheduleBlock(day, module))));
        return save(section);
    }

    public TuitionEntity tuition(StudentEntity student, AcademicPeriodEntity period){
        TuitionEntity tuition = new TuitionEntity();
        tuition.setStudent(student);
        tuition.setAcademicPeriod(period);
        tuition.setTuitionDate(LocalDate.now());
        return save(tuition);
    }

    public EnrollmentEntity enrollment(StudentEntity student, SectionEntity section, Double grade){
        EnrollmentEntity enrollment = new EnrollmentEntity();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollment.setCourse(section.getCourse());
        enrollment.setAcademicPeriod(section.getAcademicPeriod());
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setFinalGrade(grade);
        return save(enrollment);
    }
}

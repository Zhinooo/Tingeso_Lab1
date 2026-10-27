package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.AcademicPeriodDTOs.AcademicPeriodResponseDTO;
import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.EnrollmentEntity;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import SIGA_IPT.backend.mappers.AcademicPeriodMapper;
import SIGA_IPT.backend.repositories.AcademicPeriodRepository;
import SIGA_IPT.backend.repositories.CourseRepository;
import SIGA_IPT.backend.repositories.EnrollmentRepository;
import SIGA_IPT.backend.repositories.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AcademicPeriodClosingServiceTest {

    @Mock
    private AcademicPeriodRepository academicPeriodRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private StudentRepository studentRepository;

    private AcademicPeriodClosingService service;
    private AcademicPeriodEntity period;
    private StudyplanEntity plan;
    private StudentEntity student;
    private CourseEntity a1;
    private CourseEntity a2;
    private CourseEntity a3;

    @BeforeEach
    void setUp(){
        service = new AcademicPeriodClosingService(academicPeriodRepository, enrollmentRepository, courseRepository,
                studentRepository, new AcademicPeriodMapper());
        when(academicPeriodRepository.save(any(AcademicPeriodEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        period = new AcademicPeriodEntity();
        period.setAcademicPeriodId(1L);
        period.setAcademicYear(2030);
        period.setAcademicSemester(1);
        period.setAcademicPeriodStatus(AcademicPeriodEntity.AcademicPeriodStatus.OPEN);
        plan = new StudyplanEntity();
        plan.setStudyplanId(1L);
        student = new StudentEntity();
        student.setStudentId(1L);
        student.setStudentStatus(StudentEntity.StudentStatus.REGULAR);
        student.setStudyplan(plan);
        a1 = course(1L, "A1");
        a2 = course(2L, "A2");
        a3 = course(3L, "A3");

        when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(courseRepository.findByStudyplan(plan)).thenReturn(List.of(a1, a2, a3));
        when(enrollmentRepository.findByStudentAndAcademicPeriod_AcademicPeriodStatus(student,
                AcademicPeriodEntity.AcademicPeriodStatus.CLOSED)).thenReturn(new ArrayList<>());
    }

    private CourseEntity course(Long id, String code){
        CourseEntity course = new CourseEntity();
        course.setCourseId(id);
        course.setCourseCode(code);
        return course;
    }

    private EnrollmentEntity enrollment(CourseEntity course, Double grade){
        EnrollmentEntity enrollment = new EnrollmentEntity();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setFinalGrade(grade);
        return enrollment;
    }

    private void givenPeriodEnrollments(EnrollmentEntity... enrollments){
        when(enrollmentRepository.findByAcademicPeriod(period)).thenReturn(List.of(enrollments));
    }

    private void givenHistory(EnrollmentEntity... enrollments){
        when(enrollmentRepository.findByStudentAndAcademicPeriod_AcademicPeriodStatus(student,
                AcademicPeriodEntity.AcademicPeriodStatus.CLOSED)).thenReturn(List.of(enrollments));
    }

    @Test
    void closesAPeriodWithAllGradesRegistered(){
        givenPeriodEnrollments(enrollment(a1, 5.0));

        AcademicPeriodResponseDTO result = service.close(1L);

        assertThat(result.academicPeriodStatus()).isEqualTo(AcademicPeriodEntity.AcademicPeriodStatus.CLOSED);
        assertThat(student.getStudentStatus()).isEqualTo(StudentEntity.StudentStatus.REGULAR);
    }

    @Test
    void closesAPeriodWithoutEnrollments(){
        when(enrollmentRepository.findByAcademicPeriod(period)).thenReturn(List.of());

        assertThat(service.close(1L).academicPeriodStatus()).isEqualTo(AcademicPeriodEntity.AcademicPeriodStatus.CLOSED);
    }

    @Test
    void failsWhenPeriodIsMissing(){
        when(academicPeriodRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.close(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void rejectsAnAlreadyClosedPeriod(){
        period.setAcademicPeriodStatus(AcademicPeriodEntity.AcademicPeriodStatus.CLOSED);

        assertThatThrownBy(() -> service.close(1L))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("already closed");
    }

    @Test
    void rejectsClosingWhileGradesArePending(){
        givenPeriodEnrollments(enrollment(a2, null), enrollment(a1, 5.0), enrollment(a2, null), enrollment(a3, null));

        assertThatThrownBy(() -> service.close(1L))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("A2, A3");
        assertThat(period.getAcademicPeriodStatus()).isEqualTo(AcademicPeriodEntity.AcademicPeriodStatus.OPEN);
        verify(academicPeriodRepository, never()).save(any());
    }

    @Test
    void firstFailureKeepsTheStudentRegular(){
        givenPeriodEnrollments(enrollment(a1, 3.0));

        service.close(1L);

        assertThat(student.getStudentStatus()).isEqualTo(StudentEntity.StudentStatus.REGULAR);
        verify(studentRepository, never()).save(any());
    }

    @Test
    void failingTheSameCourseTwiceExpelsTheStudent(){
        givenHistory(enrollment(a1, 2.0));
        givenPeriodEnrollments(enrollment(a1, 3.5));

        service.close(1L);

        assertThat(student.getStudentStatus()).isEqualTo(StudentEntity.StudentStatus.EXPELLED);
        verify(studentRepository).save(student);
    }

    @Test
    void failingADifferentCourseAfterAFailureDoesNotExpel(){
        givenHistory(enrollment(a1, 2.0));
        givenPeriodEnrollments(enrollment(a2, 3.0));

        service.close(1L);

        assertThat(student.getStudentStatus()).isEqualTo(StudentEntity.StudentStatus.REGULAR);
    }

    @Test
    void approvingAFormerlyFailedCourseDoesNotExpel(){
        givenHistory(enrollment(a1, 2.0));
        givenPeriodEnrollments(enrollment(a1, 5.0));

        service.close(1L);

        assertThat(student.getStudentStatus()).isEqualTo(StudentEntity.StudentStatus.REGULAR);
    }

    @Test
    void approvingTheWholePlanGraduatesTheStudent(){
        givenHistory(enrollment(a1, 5.0), enrollment(a2, 6.0));
        givenPeriodEnrollments(enrollment(a3, 4.0));

        service.close(1L);

        assertThat(student.getStudentStatus()).isEqualTo(StudentEntity.StudentStatus.GRADUATED);
        verify(studentRepository).save(student);
    }

    @Test
    void approvingOnlyPartOfThePlanDoesNotGraduate(){
        givenHistory(enrollment(a1, 5.0));
        givenPeriodEnrollments(enrollment(a2, 6.0));

        service.close(1L);

        assertThat(student.getStudentStatus()).isEqualTo(StudentEntity.StudentStatus.REGULAR);
    }

    @Test
    void aPlanWithoutCoursesNeverGraduates(){
        when(courseRepository.findByStudyplan(plan)).thenReturn(List.of());
        givenPeriodEnrollments(enrollment(a1, 5.0));

        service.close(1L);

        assertThat(student.getStudentStatus()).isEqualTo(StudentEntity.StudentStatus.REGULAR);
    }

    @Test
    void expelledStudentsKeepTheirStatus(){
        student.setStudentStatus(StudentEntity.StudentStatus.EXPELLED);
        givenPeriodEnrollments(enrollment(a1, 5.0));

        service.close(1L);

        assertThat(student.getStudentStatus()).isEqualTo(StudentEntity.StudentStatus.EXPELLED);
        verify(studentRepository, never()).save(any());
    }

    @Test
    void graduatedStudentsKeepTheirStatus(){
        student.setStudentStatus(StudentEntity.StudentStatus.GRADUATED);
        givenPeriodEnrollments(enrollment(a1, 2.0));

        service.close(1L);

        assertThat(student.getStudentStatus()).isEqualTo(StudentEntity.StudentStatus.GRADUATED);
    }

    @Test
    void eachStudentIsEvaluatedSeparately(){
        StudentEntity other = new StudentEntity();
        other.setStudentId(2L);
        other.setStudentStatus(StudentEntity.StudentStatus.REGULAR);
        other.setStudyplan(plan);
        EnrollmentEntity otherEnrollment = new EnrollmentEntity();
        otherEnrollment.setStudent(other);
        otherEnrollment.setCourse(a1);
        otherEnrollment.setFinalGrade(3.0);
        when(enrollmentRepository.findByStudentAndAcademicPeriod_AcademicPeriodStatus(other,
                AcademicPeriodEntity.AcademicPeriodStatus.CLOSED)).thenReturn(List.of(otherEnrollment));
        givenPeriodEnrollments(enrollment(a1, 5.0), otherEnrollment);

        service.close(1L);

        assertThat(student.getStudentStatus()).isEqualTo(StudentEntity.StudentStatus.REGULAR);
        assertThat(other.getStudentStatus()).isEqualTo(StudentEntity.StudentStatus.EXPELLED);
    }
}

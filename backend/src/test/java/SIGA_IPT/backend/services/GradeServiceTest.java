package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.EnrollmentDTOs.EnrollmentResponseDTO;
import SIGA_IPT.backend.dtos.EnrollmentDTOs.GradeRequestDTO;
import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.EnrollmentEntity;
import SIGA_IPT.backend.entities.SectionEntity;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.entities.TeacherEntity;
import SIGA_IPT.backend.exceptions.ForbiddenOperationException;
import SIGA_IPT.backend.mappers.EnrollmentMapper;
import SIGA_IPT.backend.repositories.EnrollmentRepository;
import SIGA_IPT.backend.repositories.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GradeServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private TeacherRepository teacherRepository;

    private GradeService service;
    private EnrollmentEntity enrollment;
    private AcademicPeriodEntity period;
    private TeacherEntity teacher;

    @BeforeEach
    void setUp(){
        service = new GradeService(enrollmentRepository, teacherRepository, new EnrollmentMapper());
        when(enrollmentRepository.save(any(EnrollmentEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        teacher = new TeacherEntity();
        teacher.setTeacherId(1L);
        period = new AcademicPeriodEntity();
        period.setAcademicPeriodId(1L);
        period.setAcademicPeriodStatus(AcademicPeriodEntity.AcademicPeriodStatus.OPEN);
        CourseEntity course = new CourseEntity();
        course.setCourseId(1L);
        course.setCourseCode("A1");
        SectionEntity section = new SectionEntity();
        section.setSectionId(1L);
        section.setTeacher(teacher);
        StudentEntity student = new StudentEntity();
        student.setStudentId(1L);
        enrollment = new EnrollmentEntity();
        enrollment.setEnrollmentId(1L);
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setSection(section);
        enrollment.setAcademicPeriod(period);

        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
    }

    @Test
    void registersAnApprovingGrade(){
        EnrollmentResponseDTO result = service.registerGrade(1L, 1L, new GradeRequestDTO(5.5));

        assertThat(result.finalGrade()).isEqualTo(5.5);
        assertThat(result.result()).isEqualTo(EnrollmentEntity.EnrollmentResult.APPROVED);
    }

    @Test
    void registersAFailingGrade(){
        assertThat(service.registerGrade(1L, 1L, new GradeRequestDTO(3.9)).result())
                .isEqualTo(EnrollmentEntity.EnrollmentResult.FAILED);
    }

    @Test
    void fourIsTheMinimumPassingGrade(){
        assertThat(service.registerGrade(1L, 1L, new GradeRequestDTO(4.0)).result())
                .isEqualTo(EnrollmentEntity.EnrollmentResult.APPROVED);
    }

    @Test
    void acceptsTheBoundaryGrades(){
        assertThat(service.registerGrade(1L, 1L, new GradeRequestDTO(1.0)).finalGrade()).isEqualTo(1.0);
        assertThat(service.registerGrade(1L, 1L, new GradeRequestDTO(7.0)).finalGrade()).isEqualTo(7.0);
    }

    @Test
    void allowsCorrectingAPreviousGrade(){
        enrollment.setFinalGrade(3.0);

        assertThat(service.registerGrade(1L, 1L, new GradeRequestDTO(6.0)).finalGrade()).isEqualTo(6.0);
    }

    @Test
    void requiresAGrade(){
        assertThatThrownBy(() -> service.registerGrade(1L, 1L, new GradeRequestDTO(null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("required");
    }

    @Test
    void rejectsGradesOutOfRange(){
        assertThatThrownBy(() -> service.registerGrade(1L, 1L, new GradeRequestDTO(0.9)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("between");
        assertThatThrownBy(() -> service.registerGrade(1L, 1L, new GradeRequestDTO(7.1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNotANumberGrades(){
        assertThatThrownBy(() -> service.registerGrade(1L, 1L, new GradeRequestDTO(Double.NaN)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void failsWhenEnrollmentIsMissing(){
        when(enrollmentRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registerGrade(9L, 1L, new GradeRequestDTO(5.0)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void failsWhenTeacherIsMissing(){
        when(teacherRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registerGrade(1L, 9L, new GradeRequestDTO(5.0)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void rejectsATeacherThatIsNotInChargeOfTheSection(){
        TeacherEntity other = new TeacherEntity();
        other.setTeacherId(2L);
        when(teacherRepository.findById(2L)).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.registerGrade(1L, 2L, new GradeRequestDTO(5.0)))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void rejectsGradesInAClosedPeriod(){
        period.setAcademicPeriodStatus(AcademicPeriodEntity.AcademicPeriodStatus.CLOSED);

        assertThatThrownBy(() -> service.registerGrade(1L, 1L, new GradeRequestDTO(5.0)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("closed");
    }
}

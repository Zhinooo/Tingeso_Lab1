package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.TuitionDTOs.TuitionRequestDTO;
import SIGA_IPT.backend.dtos.TuitionDTOs.TuitionResponseDTO;
import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.entities.TuitionEntity;
import SIGA_IPT.backend.mappers.TuitionMapper;
import SIGA_IPT.backend.repositories.AcademicPeriodRepository;
import SIGA_IPT.backend.repositories.EnrollmentRepository;
import SIGA_IPT.backend.repositories.StudentRepository;
import SIGA_IPT.backend.repositories.TuitionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
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
class TuitionServiceTest {

    @Mock
    private TuitionRepository tuitionRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AcademicPeriodRepository academicPeriodRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;

    private TuitionService service;

    @BeforeEach
    void setUp(){
        service = new TuitionService(tuitionRepository, studentRepository, academicPeriodRepository,
                enrollmentRepository, new TuitionMapper());
        when(tuitionRepository.save(any(TuitionEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private StudentEntity student(StudentEntity.StudentStatus status){
        StudentEntity student = new StudentEntity();
        student.setStudentId(1L);
        student.setStudentRun("1-9");
        student.setStudentStatus(status);
        return student;
    }

    private AcademicPeriodEntity period(AcademicPeriodEntity.AcademicPeriodStatus status){
        AcademicPeriodEntity period = new AcademicPeriodEntity();
        period.setAcademicPeriodId(1L);
        period.setAcademicYear(2030);
        period.setAcademicSemester(1);
        period.setAcademicPeriodStatus(status);
        return period;
    }

    private TuitionEntity tuition(StudentEntity student, AcademicPeriodEntity period){
        TuitionEntity tuition = new TuitionEntity();
        tuition.setTuitionId(1L);
        tuition.setStudent(student);
        tuition.setAcademicPeriod(period);
        tuition.setTuitionDate(LocalDate.now());
        return tuition;
    }

    private void givenStudentAndPeriod(StudentEntity student, AcademicPeriodEntity period){
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
    }

    @Test
    void createRegistersTuitionForRegularStudent(){
        givenStudentAndPeriod(student(StudentEntity.StudentStatus.REGULAR), period(AcademicPeriodEntity.AcademicPeriodStatus.OPEN));

        TuitionResponseDTO result = service.create(new TuitionRequestDTO(1L, 1L));

        assertThat(result.studentId()).isEqualTo(1L);
        assertThat(result.tuitionDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void createRequiresStudent(){
        assertThatThrownBy(() -> service.create(new TuitionRequestDTO(null, 1L)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("student");
    }

    @Test
    void createRequiresAcademicPeriod(){
        assertThatThrownBy(() -> service.create(new TuitionRequestDTO(1L, null)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("academic period");
    }

    @Test
    void createFailsWhenStudentIsMissing(){
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new TuitionRequestDTO(1L, 1L))).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createFailsWhenPeriodIsMissing(){
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(StudentEntity.StudentStatus.REGULAR)));
        when(academicPeriodRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new TuitionRequestDTO(1L, 1L))).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createRejectsClosedPeriod(){
        givenStudentAndPeriod(student(StudentEntity.StudentStatus.REGULAR), period(AcademicPeriodEntity.AcademicPeriodStatus.CLOSED));

        assertThatThrownBy(() -> service.create(new TuitionRequestDTO(1L, 1L)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("closed");
    }

    @Test
    void createRejectsGraduatedStudent(){
        givenStudentAndPeriod(student(StudentEntity.StudentStatus.GRADUATED), period(AcademicPeriodEntity.AcademicPeriodStatus.OPEN));

        assertThatThrownBy(() -> service.create(new TuitionRequestDTO(1L, 1L)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("GRADUATED");
    }

    @Test
    void createRejectsExpelledStudent(){
        givenStudentAndPeriod(student(StudentEntity.StudentStatus.EXPELLED), period(AcademicPeriodEntity.AcademicPeriodStatus.OPEN));

        assertThatThrownBy(() -> service.create(new TuitionRequestDTO(1L, 1L)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("EXPELLED");
    }

    @Test
    void createRejectsSecondTuitionInSamePeriod(){
        StudentEntity student = student(StudentEntity.StudentStatus.REGULAR);
        AcademicPeriodEntity period = period(AcademicPeriodEntity.AcademicPeriodStatus.OPEN);
        givenStudentAndPeriod(student, period);
        when(tuitionRepository.existsByStudentAndAcademicPeriod(student, period)).thenReturn(true);

        assertThatThrownBy(() -> service.create(new TuitionRequestDTO(1L, 1L)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("already has a tuition");
        verify(tuitionRepository, never()).save(any());
    }

    @Test
    void getAllReturnsTuitions(){
        when(tuitionRepository.findAll()).thenReturn(List.of(
                tuition(student(StudentEntity.StudentStatus.REGULAR), period(AcademicPeriodEntity.AcademicPeriodStatus.OPEN))));

        assertThat(service.getAll()).hasSize(1);
    }

    @Test
    void getByIdReturnsTuition(){
        when(tuitionRepository.findById(1L)).thenReturn(Optional.of(
                tuition(student(StudentEntity.StudentStatus.REGULAR), period(AcademicPeriodEntity.AcademicPeriodStatus.OPEN))));

        assertThat(service.getById(1L).tuitionId()).isEqualTo(1L);
    }

    @Test
    void getByIdFailsWhenMissing(){
        when(tuitionRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getByStudentReturnsHisTuitions(){
        StudentEntity student = student(StudentEntity.StudentStatus.REGULAR);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(tuitionRepository.findByStudent(student)).thenReturn(List.of(
                tuition(student, period(AcademicPeriodEntity.AcademicPeriodStatus.OPEN))));

        assertThat(service.getByStudent(1L)).hasSize(1);
    }

    @Test
    void getByStudentFailsWhenStudentIsMissing(){
        when(studentRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByStudent(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getByAcademicPeriodReturnsTuitions(){
        StudentEntity student = student(StudentEntity.StudentStatus.REGULAR);
        AcademicPeriodEntity period = period(AcademicPeriodEntity.AcademicPeriodStatus.OPEN);
        when(academicPeriodRepository.findById(1L)).thenReturn(Optional.of(period));
        when(tuitionRepository.findByAcademicPeriod(period)).thenReturn(List.of(tuition(student, period)));

        assertThat(service.getByAcademicPeriod(1L)).hasSize(1);
    }

    @Test
    void getByAcademicPeriodFailsWhenPeriodIsMissing(){
        when(academicPeriodRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByAcademicPeriod(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteRemovesTuitionOfOpenPeriod(){
        TuitionEntity tuition = tuition(student(StudentEntity.StudentStatus.REGULAR), period(AcademicPeriodEntity.AcademicPeriodStatus.OPEN));
        when(tuitionRepository.findById(1L)).thenReturn(Optional.of(tuition));

        service.delete(1L);

        verify(tuitionRepository).delete(tuition);
    }

    @Test
    void deleteRejectsClosedPeriod(){
        TuitionEntity tuition = tuition(student(StudentEntity.StudentStatus.REGULAR), period(AcademicPeriodEntity.AcademicPeriodStatus.CLOSED));
        when(tuitionRepository.findById(1L)).thenReturn(Optional.of(tuition));

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(IllegalStateException.class).hasMessageContaining("closed");
    }

    @Test
    void deleteRejectsWhenStudentHasEnrollmentsInThePeriod(){
        TuitionEntity tuition = tuition(student(StudentEntity.StudentStatus.REGULAR), period(AcademicPeriodEntity.AcademicPeriodStatus.OPEN));
        when(tuitionRepository.findById(1L)).thenReturn(Optional.of(tuition));
        when(enrollmentRepository.existsByStudentAndAcademicPeriod(tuition.getStudent(), tuition.getAcademicPeriod())).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(IllegalStateException.class).hasMessageContaining("enrollments");
        verify(tuitionRepository, never()).delete(any());
    }
}

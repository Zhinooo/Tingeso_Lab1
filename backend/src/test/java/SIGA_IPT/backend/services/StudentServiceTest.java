package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.StudentDTOs.StudentCareerChangeRequestDTO;
import SIGA_IPT.backend.dtos.StudentDTOs.StudentRequestDTO;
import SIGA_IPT.backend.dtos.StudentDTOs.StudentResponseDTO;
import SIGA_IPT.backend.dtos.StudentDTOs.StudentUpdateRequestDTO;
import SIGA_IPT.backend.entities.CareerEntity;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import SIGA_IPT.backend.identity.UserProvisioner;
import SIGA_IPT.backend.mappers.StudentMapper;
import SIGA_IPT.backend.repositories.CareerRepository;
import SIGA_IPT.backend.repositories.EnrollmentRepository;
import SIGA_IPT.backend.repositories.StudentRepository;
import SIGA_IPT.backend.repositories.StudyplanRepository;
import SIGA_IPT.backend.repositories.TuitionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CareerRepository careerRepository;
    @Mock
    private StudyplanRepository studyplanRepository;
    @Mock
    private TuitionRepository tuitionRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private UserProvisioner userProvisioner;

    private StudentService service;
    private CareerEntity career;
    private StudyplanEntity currentPlan;

    @BeforeEach
    void setUp(){
        service = new StudentService(studentRepository, careerRepository, studyplanRepository, tuitionRepository,
                enrollmentRepository, new StudentMapper(), userProvisioner);
        when(studentRepository.save(any(StudentEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        career = new CareerEntity();
        career.setCareerId(1L);
        career.setCareerCode("10450");
        career.setCareerStatus(CareerEntity.CareerStatus.ACTIVE);
        currentPlan = new StudyplanEntity();
        currentPlan.setStudyplanId(1L);
        currentPlan.setStudyplanIdentifier("2021.3");
        currentPlan.setCareer(career);
        currentPlan.setStudyplanStatus(StudyplanEntity.StudyplanStatus.CURRENT);
        when(careerRepository.findByCareerCode("10450")).thenReturn(Optional.of(career));
        when(studyplanRepository.findByCareerAndStudyplanStatus(career, StudyplanEntity.StudyplanStatus.CURRENT))
                .thenReturn(Optional.of(currentPlan));
    }

    private StudentEntity student(StudentEntity.StudentStatus status){
        StudentEntity student = new StudentEntity();
        student.setStudentId(1L);
        student.setStudentRun("1-9");
        student.setStudentFullname("Student One");
        student.setStudentEmail("s1@test.cl");
        student.setCareer(career);
        student.setStudyplan(currentPlan);
        student.setStudentStatus(status);
        return student;
    }

    private StudentRequestDTO request(String run, String name, String email, String careerCode){
        return new StudentRequestDTO(run, name, email, careerCode, "Initial123");
    }

    @Test
    void createRegistersRegularStudentWithTheCurrentPlan(){
        StudentResponseDTO result = service.create(request("1-9", "Student", "s@test.cl", "10450"));

        assertThat(result.studentStatus()).isEqualTo(StudentEntity.StudentStatus.REGULAR);
        assertThat(result.studyplanIdentifier()).isEqualTo("2021.3");
        assertThat(result.careerCode()).isEqualTo("10450");
    }

    @Test
    void createProvisionsTheIdentityProviderUserWithTheStudentRole(){
        service.create(new StudentRequestDTO("1-9", "Student", "s@test.cl", "10450", "Initial123"));

        verify(userProvisioner).create("s@test.cl", "Student", "Initial123", "STUDENT");
    }

    @Test
    void createRequiresAnInitialPassword(){
        assertThatThrownBy(() -> service.create(new StudentRequestDTO("1", "S", "e", "10450", " ")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("password is required");
        assertThatThrownBy(() -> service.create(new StudentRequestDTO("1", "S", "e", "10450", null)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(userProvisioner, never()).create(any(), any(), any(), any());
    }

    @Test
    void createRejectsAShortPassword(){
        assertThatThrownBy(() -> service.create(new StudentRequestDTO("1", "S", "e", "10450", "short")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("at least 8");
    }

    @Test
    void createDoesNotReachTheIdentityProviderWhenARuleFails(){
        when(studentRepository.existsByStudentRun("1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("1", "S", "e", "10450"))).isInstanceOf(IllegalArgumentException.class);
        verify(userProvisioner, never()).create(any(), any(), any(), any());
    }

    @Test
    void createFailsWhenTheIdentityProviderFails(){
        org.mockito.Mockito.doThrow(new SIGA_IPT.backend.exceptions.IdentityProviderException("down", null))
                .when(userProvisioner).create(any(), any(), any(), any());

        assertThatThrownBy(() -> service.create(request("1-9", "S", "e", "10450")))
                .isInstanceOf(SIGA_IPT.backend.exceptions.IdentityProviderException.class);
    }

    @Test
    void createRejectsBlankRun(){
        assertThatThrownBy(() -> service.create(request(" ", "S", "e", "10450")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("RUN");
    }

    @Test
    void createRejectsBlankCareerCode(){
        assertThatThrownBy(() -> service.create(request("1", "S", "e", "")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("career code");
    }

    @Test
    void createRejectsMissingName(){
        assertThatThrownBy(() -> service.create(request("1", null, "e", "10450")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("full name");
    }

    @Test
    void createRejectsMissingEmail(){
        assertThatThrownBy(() -> service.create(request("1", "S", " ", "10450")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("email");
    }

    @Test
    void createRejectsDuplicatedRun(){
        when(studentRepository.existsByStudentRun("1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("1", "S", "e", "10450")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("RUN already exists");
        verify(studentRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicatedEmail(){
        when(studentRepository.existsByStudentEmail("e")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("1", "S", "e", "10450")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("email already exists");
    }

    @Test
    void createFailsWhenCareerDoesNotExist(){
        when(careerRepository.findByCareerCode("X")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request("1", "S", "e", "X"))).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createRejectsInactiveCareer(){
        career.setCareerStatus(CareerEntity.CareerStatus.INACTIVE);

        assertThatThrownBy(() -> service.create(request("1", "S", "e", "10450")))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("not active");
    }

    @Test
    void createRejectsCareerWithoutCurrentPlan(){
        when(studyplanRepository.findByCareerAndStudyplanStatus(career, StudyplanEntity.StudyplanStatus.CURRENT))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request("1", "S", "e", "10450")))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("no current study plan");
    }

    @Test
    void getAllReturnsStudents(){
        when(studentRepository.findAll()).thenReturn(List.of(student(StudentEntity.StudentStatus.REGULAR)));

        assertThat(service.getAll()).hasSize(1);
    }

    @Test
    void getByIdReturnsStudent(){
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(StudentEntity.StudentStatus.REGULAR)));

        assertThat(service.getById(1L).studentRun()).isEqualTo("1-9");
    }

    @Test
    void getByIdFailsWhenMissing(){
        when(studentRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateChangesNameAndEmail(){
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(StudentEntity.StudentStatus.REGULAR)));

        StudentResponseDTO result = service.update(1L, new StudentUpdateRequestDTO("New Name", "new@test.cl"));

        assertThat(result.studentFullname()).isEqualTo("New Name");
        assertThat(result.studentEmail()).isEqualTo("new@test.cl");
    }

    @Test
    void updateSyncsTheProfileWithTheIdentityProvider(){
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(StudentEntity.StudentStatus.REGULAR)));

        service.update(1L, new StudentUpdateRequestDTO("New Name", "new@test.cl"));

        verify(userProvisioner).updateProfile("s1@test.cl", "new@test.cl", "New Name");
    }

    @Test
    void updateKeepingTheEmailSkipsTheDuplicateCheck(){
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(StudentEntity.StudentStatus.REGULAR)));
        when(studentRepository.existsByStudentEmail("s1@test.cl")).thenReturn(true);

        assertThat(service.update(1L, new StudentUpdateRequestDTO("Other", "s1@test.cl")).studentFullname()).isEqualTo("Other");
    }

    @Test
    void updateRejectsEmailOfAnotherStudent(){
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(StudentEntity.StudentStatus.REGULAR)));
        when(studentRepository.existsByStudentEmail("taken@test.cl")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new StudentUpdateRequestDTO("N", "taken@test.cl")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateRejectsInvalidFields(){
        assertThatThrownBy(() -> service.update(1L, new StudentUpdateRequestDTO("", "e")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changeCareerUsesTheCurrentPlanWhenNoIdentifierIsGiven(){
        CareerEntity other = new CareerEntity();
        other.setCareerCode("10451");
        other.setCareerStatus(CareerEntity.CareerStatus.ACTIVE);
        StudyplanEntity otherPlan = new StudyplanEntity();
        otherPlan.setStudyplanIdentifier("2022.3");
        otherPlan.setCareer(other);
        when(careerRepository.findByCareerCode("10451")).thenReturn(Optional.of(other));
        when(studyplanRepository.findByCareerAndStudyplanStatus(other, StudyplanEntity.StudyplanStatus.CURRENT))
                .thenReturn(Optional.of(otherPlan));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(StudentEntity.StudentStatus.REGULAR)));

        StudentResponseDTO result = service.changeCareerAndPlan(1L, new StudentCareerChangeRequestDTO("10451", null));

        assertThat(result.careerCode()).isEqualTo("10451");
        assertThat(result.studyplanIdentifier()).isEqualTo("2022.3");
    }

    @Test
    void changeCareerUsesTheRequestedPlanWhenIdentifierIsGiven(){
        StudyplanEntity oldPlan = new StudyplanEntity();
        oldPlan.setStudyplanIdentifier("2019.1");
        oldPlan.setCareer(career);
        when(studyplanRepository.findByStudyplanIdentifierAndCareer("2019.1", career)).thenReturn(Optional.of(oldPlan));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(StudentEntity.StudentStatus.REGULAR)));

        assertThat(service.changeCareerAndPlan(1L, new StudentCareerChangeRequestDTO("10450", "2019.1")).studyplanIdentifier())
                .isEqualTo("2019.1");
    }

    @Test
    void changeCareerFailsWhenRequestedPlanDoesNotExist(){
        when(studyplanRepository.findByStudyplanIdentifierAndCareer("NOPE", career)).thenReturn(Optional.empty());
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(StudentEntity.StudentStatus.REGULAR)));

        assertThatThrownBy(() -> service.changeCareerAndPlan(1L, new StudentCareerChangeRequestDTO("10450", "NOPE")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void changeCareerRequiresCareerCode(){
        assertThatThrownBy(() -> service.changeCareerAndPlan(1L, new StudentCareerChangeRequestDTO(" ", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changeCareerRejectsStudentsWithEnrollments(){
        StudentEntity student = student(StudentEntity.StudentStatus.REGULAR);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByStudent(student)).thenReturn(true);

        assertThatThrownBy(() -> service.changeCareerAndPlan(1L, new StudentCareerChangeRequestDTO("10450", null)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("enrollments");
    }

    @Test
    void changeStatusAllowsDeferred(){
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(StudentEntity.StudentStatus.REGULAR)));

        assertThat(service.changeStatus(1L, StudentEntity.StudentStatus.DEFERRED).studentStatus())
                .isEqualTo(StudentEntity.StudentStatus.DEFERRED);
    }

    @Test
    void changeStatusAllowsReturningToRegular(){
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(StudentEntity.StudentStatus.TEMPORARY_WITHDRAWAL)));

        assertThat(service.changeStatus(1L, StudentEntity.StudentStatus.REGULAR).studentStatus())
                .isEqualTo(StudentEntity.StudentStatus.REGULAR);
    }

    @Test
    void changeStatusRejectsNull(){
        assertThatThrownBy(() -> service.changeStatus(1L, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changeStatusRejectsManualGraduation(){
        assertThatThrownBy(() -> service.changeStatus(1L, StudentEntity.StudentStatus.GRADUATED))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("manually");
    }

    @Test
    void changeStatusRejectsManualExpulsion(){
        assertThatThrownBy(() -> service.changeStatus(1L, StudentEntity.StudentStatus.EXPELLED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changeStatusRejectsGraduatedStudents(){
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(StudentEntity.StudentStatus.GRADUATED)));

        assertThatThrownBy(() -> service.changeStatus(1L, StudentEntity.StudentStatus.REGULAR))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("GRADUATED");
    }

    @Test
    void changeStatusRejectsExpelledStudents(){
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student(StudentEntity.StudentStatus.EXPELLED)));

        assertThatThrownBy(() -> service.changeStatus(1L, StudentEntity.StudentStatus.REGULAR))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("EXPELLED");
    }

    @Test
    void deleteRemovesStudentWithoutAssociations(){
        StudentEntity student = student(StudentEntity.StudentStatus.REGULAR);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        service.delete(1L);

        verify(studentRepository).delete(student);
        verify(userProvisioner).delete("s1@test.cl");
    }

    @Test
    void deleteRejectsStudentWithTuitions(){
        StudentEntity student = student(StudentEntity.StudentStatus.REGULAR);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(tuitionRepository.existsByStudent(student)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(IllegalStateException.class).hasMessageContaining("tuitions");
    }

    @Test
    void deleteRejectsStudentWithEnrollments(){
        StudentEntity student = student(StudentEntity.StudentStatus.REGULAR);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByStudent(student)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(IllegalStateException.class).hasMessageContaining("enrollments");
        verify(studentRepository, never()).delete(any());
        verify(userProvisioner, never()).delete(any());
    }
}

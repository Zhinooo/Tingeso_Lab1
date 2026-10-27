package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.TeacherDTOs.TeacherRequestDTO;
import SIGA_IPT.backend.dtos.TeacherDTOs.TeacherResponseDTO;
import SIGA_IPT.backend.dtos.TeacherDTOs.TeacherUpdateRequestDTO;
import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.TeacherEntity;
import SIGA_IPT.backend.identity.UserProvisioner;
import SIGA_IPT.backend.mappers.TeacherMapper;
import SIGA_IPT.backend.repositories.SectionRepository;
import SIGA_IPT.backend.repositories.TeacherRepository;
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
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private SectionRepository sectionRepository;
    @Mock
    private UserProvisioner userProvisioner;

    private TeacherService service;

    @BeforeEach
    void setUp(){
        service = new TeacherService(teacherRepository, sectionRepository, new TeacherMapper(), userProvisioner);
        when(teacherRepository.save(any(TeacherEntity.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private TeacherEntity teacher(){
        TeacherEntity teacher = new TeacherEntity();
        teacher.setTeacherId(1L);
        teacher.setTeacherRun("11-1");
        teacher.setTeacherFullname("Teacher One");
        teacher.setTeacherEmail("t1@test.cl");
        teacher.setTeacherProtitle("Engineer");
        teacher.setTeacherDegree("Master");
        teacher.setTeacherStatus(TeacherEntity.TeacherStatus.ACTIVE);
        return teacher;
    }

    private TeacherRequestDTO request(String run, String name, String email, String title, String degree){
        return new TeacherRequestDTO(run, name, email, title, degree, "Initial123");
    }

    @Test
    void createSavesActiveTeacher(){
        TeacherResponseDTO result = service.create(request("11-1", "Teacher", "t@test.cl", "Eng", "MSc"));

        assertThat(result.teacherStatus()).isEqualTo(TeacherEntity.TeacherStatus.ACTIVE);
        assertThat(result.teacherEmail()).isEqualTo("t@test.cl");
    }

    @Test
    void createProvisionsTheIdentityProviderUserWithTheTeacherRole(){
        service.create(request("11-1", "Teacher", "t@test.cl", "Eng", "MSc"));

        verify(userProvisioner).create("t@test.cl", "Teacher", "Initial123", "TEACHER");
    }

    @Test
    void createRequiresAnInitialPassword(){
        assertThatThrownBy(() -> service.create(new TeacherRequestDTO("1", "T", "e", "p", "d", "")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("password is required");
        assertThatThrownBy(() -> service.create(new TeacherRequestDTO("1", "T", "e", "p", "d", null)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(userProvisioner, never()).create(any(), any(), any(), any());
    }

    @Test
    void createRejectsAShortPassword(){
        assertThatThrownBy(() -> service.create(new TeacherRequestDTO("1", "T", "e", "p", "d", "1234567")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("at least 8");
    }

    @Test
    void createFailsWhenTheIdentityProviderFails(){
        org.mockito.Mockito.doThrow(new SIGA_IPT.backend.exceptions.IdentityProviderException("down", null))
                .when(userProvisioner).create(any(), any(), any(), any());

        assertThatThrownBy(() -> service.create(request("1", "T", "e", "p", "d")))
                .isInstanceOf(SIGA_IPT.backend.exceptions.IdentityProviderException.class);
    }

    @Test
    void createRejectsBlankRun(){
        assertThatThrownBy(() -> service.create(request(" ", "T", "e", "p", "d")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("RUN");
    }

    @Test
    void createRejectsMissingName(){
        assertThatThrownBy(() -> service.create(request("1", null, "e", "p", "d")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("full name");
    }

    @Test
    void createRejectsMissingEmail(){
        assertThatThrownBy(() -> service.create(request("1", "T", "", "p", "d")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("email");
    }

    @Test
    void createRejectsMissingProfessionalTitle(){
        assertThatThrownBy(() -> service.create(request("1", "T", "e", null, "d")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("professional title");
    }

    @Test
    void createRejectsMissingDegree(){
        assertThatThrownBy(() -> service.create(request("1", "T", "e", "p", " ")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("degree");
    }

    @Test
    void createRejectsDuplicatedRun(){
        when(teacherRepository.existsByTeacherRun("1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("1", "T", "e", "p", "d")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("RUN already exists");
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicatedEmail(){
        when(teacherRepository.existsByTeacherEmail("e")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("1", "T", "e", "p", "d")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("email already exists");
    }

    @Test
    void getAllReturnsTeachers(){
        when(teacherRepository.findAll()).thenReturn(List.of(teacher()));

        assertThat(service.getAll()).hasSize(1);
    }

    @Test
    void getByIdReturnsTeacher(){
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher()));

        assertThat(service.getById(1L).teacherRun()).isEqualTo("11-1");
    }

    @Test
    void getByIdFailsWhenMissing(){
        when(teacherRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(9L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateChangesProfile(){
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher()));

        TeacherResponseDTO result = service.update(1L, new TeacherUpdateRequestDTO("New Name", "t1@test.cl", "Eng", "PhD"));

        assertThat(result.teacherFullname()).isEqualTo("New Name");
        assertThat(result.teacherDegree()).isEqualTo("PhD");
    }

    @Test
    void updateSyncsTheProfileWithTheIdentityProvider(){
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher()));

        service.update(1L, new TeacherUpdateRequestDTO("New Name", "new@test.cl", "E", "D"));

        verify(userProvisioner).updateProfile("t1@test.cl", "new@test.cl", "New Name");
    }

    @Test
    void updateAllowsAFreeNewEmail(){
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher()));
        when(teacherRepository.existsByTeacherEmail("new@test.cl")).thenReturn(false);

        assertThat(service.update(1L, new TeacherUpdateRequestDTO("N", "new@test.cl", "E", "D")).teacherEmail())
                .isEqualTo("new@test.cl");
    }

    @Test
    void updateRejectsEmailOfAnotherTeacher(){
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher()));
        when(teacherRepository.existsByTeacherEmail("other@test.cl")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new TeacherUpdateRequestDTO("N", "other@test.cl", "E", "D")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateRejectsInvalidFields(){
        assertThatThrownBy(() -> service.update(1L, new TeacherUpdateRequestDTO(null, "e", "p", "d")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changeStatusDeactivatesTeacherWithoutOpenSections(){
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher()));

        assertThat(service.changeStatus(1L, TeacherEntity.TeacherStatus.INACTIVE).teacherStatus())
                .isEqualTo(TeacherEntity.TeacherStatus.INACTIVE);
    }

    @Test
    void changeStatusRejectsDeactivatingTeacherWithOpenSections(){
        TeacherEntity teacher = teacher();
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsByTeacherAndAcademicPeriod_AcademicPeriodStatus(
                teacher, AcademicPeriodEntity.AcademicPeriodStatus.OPEN)).thenReturn(true);

        assertThatThrownBy(() -> service.changeStatus(1L, TeacherEntity.TeacherStatus.INACTIVE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void changeStatusActivatesWithoutCheckingSections(){
        TeacherEntity teacher = teacher();
        teacher.setTeacherStatus(TeacherEntity.TeacherStatus.INACTIVE);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        assertThat(service.changeStatus(1L, TeacherEntity.TeacherStatus.ACTIVE).teacherStatus())
                .isEqualTo(TeacherEntity.TeacherStatus.ACTIVE);
    }

    @Test
    void changeStatusRejectsNull(){
        assertThatThrownBy(() -> service.changeStatus(1L, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteRemovesTeacherWithoutSections(){
        TeacherEntity teacher = teacher();
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));

        service.delete(1L);

        verify(teacherRepository).delete(teacher);
        verify(userProvisioner).delete("t1@test.cl");
    }

    @Test
    void deleteRejectsTeacherWithSections(){
        TeacherEntity teacher = teacher();
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsByTeacher(teacher)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(IllegalStateException.class);
        verify(teacherRepository, never()).delete(any());
        verify(userProvisioner, never()).delete(any());
    }
}

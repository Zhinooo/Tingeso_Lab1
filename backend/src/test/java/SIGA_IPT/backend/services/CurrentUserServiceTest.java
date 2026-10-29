package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.UserDTOs.MeResponseDTO;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.entities.TeacherEntity;
import SIGA_IPT.backend.exceptions.ForbiddenOperationException;
import SIGA_IPT.backend.repositories.StudentRepository;
import SIGA_IPT.backend.repositories.TeacherRepository;
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
class CurrentUserServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private TeacherRepository teacherRepository;

    private CurrentUserService service;

    @BeforeEach
    void setUp(){
        service = new CurrentUserService(studentRepository, teacherRepository);
        StudentEntity student = new StudentEntity();
        student.setStudentId(5L);
        student.setStudentFullname("Student Five");
        TeacherEntity teacher = new TeacherEntity();
        teacher.setTeacherId(7L);
        teacher.setTeacherFullname("Teacher Seven");
        when(studentRepository.findByKeycloakUserId("kc-student")).thenReturn(Optional.of(student));
        when(teacherRepository.findByKeycloakUserId("kc-teacher")).thenReturn(Optional.of(teacher));
        when(studentRepository.findByKeycloakUserId("")).thenReturn(Optional.empty());
        when(teacherRepository.findByKeycloakUserId("")).thenReturn(Optional.empty());
    }

    @Test
    void requireStudentIdReturnsTheLinkedStudent(){
        assertThat(service.requireStudentId("kc-student")).isEqualTo(5L);
    }

    @Test
    void requireStudentIdRejectsUnknownUsers(){
        when(studentRepository.findByKeycloakUserId("kc-unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requireStudentId("kc-unknown")).isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireStudentIdRejectsMissingUserId(){
        assertThatThrownBy(() -> service.requireStudentId(null)).isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireTeacherIdReturnsTheLinkedTeacher(){
        assertThat(service.requireTeacherId("kc-teacher")).isEqualTo(7L);
    }

    @Test
    void requireTeacherIdRejectsUnknownUsers(){
        when(teacherRepository.findByKeycloakUserId("kc-unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requireTeacherId("kc-unknown")).isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireTeacherIdRejectsMissingUserId(){
        assertThatThrownBy(() -> service.requireTeacherId(null)).isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void getMeUsesTheStudentRecord(){
        MeResponseDTO me = service.getMe("kc-student", "s@siga.cl", "student", List.of("STUDENT"));

        assertThat(me.studentId()).isEqualTo(5L);
        assertThat(me.teacherId()).isNull();
        assertThat(me.fullName()).isEqualTo("Student Five");
        assertThat(me.email()).isEqualTo("s@siga.cl");
        assertThat(me.roles()).containsExactly("STUDENT");
    }

    @Test
    void getMeUsesTheTeacherRecord(){
        MeResponseDTO me = service.getMe("kc-teacher", "t@siga.cl", "teacher", List.of("TEACHER"));

        assertThat(me.teacherId()).isEqualTo(7L);
        assertThat(me.studentId()).isNull();
        assertThat(me.fullName()).isEqualTo("Teacher Seven");
    }

    @Test
    void getMeOnlyReturnsTheApplicationRoles(){
        MeResponseDTO me = service.getMe("kc-student", "s@siga.cl", "student",
                List.of("STUDENT", "default-roles-siga", "offline_access", "uma_authorization", "FACTOR_BEARER"));

        assertThat(me.roles()).containsExactly("STUDENT");
    }

    @Test
    void getMeReturnsEmptyRolesWhenThereIsNoApplicationRole(){
        assertThat(service.getMe("kc-student", "s@siga.cl", "student", List.of("offline_access")).roles()).isEmpty();
    }

    @Test
    void getMeFallsBackToTheUsernameWithoutARecord(){
        when(studentRepository.findByKeycloakUserId("kc-admin")).thenReturn(Optional.empty());
        when(teacherRepository.findByKeycloakUserId("kc-admin")).thenReturn(Optional.empty());

        MeResponseDTO me = service.getMe("kc-admin", "a@siga.cl", "admin", List.of("ADMIN"));

        assertThat(me.fullName()).isEqualTo("admin");
        assertThat(me.email()).isEqualTo("a@siga.cl");
        assertThat(me.studentId()).isNull();
        assertThat(me.teacherId()).isNull();
    }

    @Test
    void getMeWithoutUserIdDoesNotQueryTheRecords(){
        MeResponseDTO me = service.getMe(null, null, "admin", List.of("ADMIN"));

        assertThat(me.fullName()).isEqualTo("admin");
        assertThat(me.email()).isNull();
        verify(studentRepository, never()).findByKeycloakUserId(any());
        verify(teacherRepository, never()).findByKeycloakUserId(any());
    }
}

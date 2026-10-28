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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CurrentUserServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private TeacherRepository teacherRepository;

    private CurrentUserService service;
    private StudentEntity student;
    private TeacherEntity teacher;

    @BeforeEach
    void setUp(){
        service = new CurrentUserService(studentRepository, teacherRepository);
        student = new StudentEntity();
        student.setStudentId(5L);
        student.setStudentFullname("Student Five");
        teacher = new TeacherEntity();
        teacher.setTeacherId(7L);
        teacher.setTeacherFullname("Teacher Seven");
        when(studentRepository.findByStudentEmail("s@siga.cl")).thenReturn(Optional.of(student));
        when(teacherRepository.findByTeacherEmail("t@siga.cl")).thenReturn(Optional.of(teacher));
        when(studentRepository.findByStudentEmail("")).thenReturn(Optional.empty());
        when(teacherRepository.findByTeacherEmail("")).thenReturn(Optional.empty());
    }

    @Test
    void requireStudentIdReturnsTheLinkedStudent(){
        assertThat(service.requireStudentId("s@siga.cl")).isEqualTo(5L);
    }

    @Test
    void requireStudentIdRejectsUnknownEmails(){
        when(studentRepository.findByStudentEmail("x@siga.cl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requireStudentId("x@siga.cl")).isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireStudentIdRejectsMissingEmail(){
        assertThatThrownBy(() -> service.requireStudentId(null)).isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireTeacherIdReturnsTheLinkedTeacher(){
        assertThat(service.requireTeacherId("t@siga.cl")).isEqualTo(7L);
    }

    @Test
    void requireTeacherIdRejectsUnknownEmails(){
        when(teacherRepository.findByTeacherEmail("x@siga.cl")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requireTeacherId("x@siga.cl")).isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireTeacherIdRejectsMissingEmail(){
        assertThatThrownBy(() -> service.requireTeacherId(null)).isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void getMeUsesTheStudentRecord(){
        MeResponseDTO me = service.getMe("s@siga.cl", "student", List.of("STUDENT"));

        assertThat(me.studentId()).isEqualTo(5L);
        assertThat(me.teacherId()).isNull();
        assertThat(me.fullName()).isEqualTo("Student Five");
        assertThat(me.roles()).containsExactly("STUDENT");
    }

    @Test
    void getMeUsesTheTeacherRecord(){
        MeResponseDTO me = service.getMe("t@siga.cl", "teacher", List.of("TEACHER"));

        assertThat(me.teacherId()).isEqualTo(7L);
        assertThat(me.studentId()).isNull();
        assertThat(me.fullName()).isEqualTo("Teacher Seven");
    }

    @Test
    void getMeOnlyReturnsTheApplicationRoles(){
        MeResponseDTO me = service.getMe("s@siga.cl", "student",
                List.of("STUDENT", "default-roles-siga", "offline_access", "uma_authorization", "FACTOR_BEARER"));

        assertThat(me.roles()).containsExactly("STUDENT");
    }

    @Test
    void getMeReturnsEmptyRolesWhenThereIsNoApplicationRole(){
        assertThat(service.getMe("s@siga.cl", "student", List.of("offline_access")).roles()).isEmpty();
    }

    @Test
    void getMeFallsBackToTheUsernameWithoutARecord(){
        when(studentRepository.findByStudentEmail("a@siga.cl")).thenReturn(Optional.empty());
        when(teacherRepository.findByTeacherEmail("a@siga.cl")).thenReturn(Optional.empty());

        MeResponseDTO me = service.getMe("a@siga.cl", "admin", List.of("ADMIN"));

        assertThat(me.fullName()).isEqualTo("admin");
        assertThat(me.studentId()).isNull();
        assertThat(me.teacherId()).isNull();
    }

    @Test
    void getMeWithoutEmailDoesNotQueryTheRecords(){
        MeResponseDTO me = service.getMe(null, "admin", List.of("ADMIN"));

        assertThat(me.fullName()).isEqualTo("admin");
        assertThat(me.email()).isNull();
    }
}

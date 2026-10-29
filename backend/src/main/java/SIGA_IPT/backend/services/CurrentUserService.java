package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.UserDTOs.MeResponseDTO;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.entities.TeacherEntity;
import SIGA_IPT.backend.exceptions.ForbiddenOperationException;
import SIGA_IPT.backend.repositories.StudentRepository;
import SIGA_IPT.backend.repositories.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class CurrentUserService {

    private static final Set<String> APPLICATION_ROLES = Set.of("ADMIN", "TEACHER", "STUDENT");

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public CurrentUserService(StudentRepository studentRepository,
                              TeacherRepository teacherRepository){
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    public Long requireStudentId(String keycloakUserId){
        return studentRepository.findByKeycloakUserId(keycloakUserId == null ? "" : keycloakUserId)
                .map(StudentEntity::getStudentId)
                .orElseThrow(() -> new ForbiddenOperationException("No student record is linked to the authenticated user."));
    }

    public Long requireTeacherId(String keycloakUserId){
        return teacherRepository.findByKeycloakUserId(keycloakUserId == null ? "" : keycloakUserId)
                .map(TeacherEntity::getTeacherId)
                .orElseThrow(() -> new ForbiddenOperationException("No teacher record is linked to the authenticated user."));
    }

    public MeResponseDTO getMe(String keycloakUserId, String email, String username, List<String> roles){
        StudentEntity student = keycloakUserId == null ? null : studentRepository.findByKeycloakUserId(keycloakUserId).orElse(null);
        TeacherEntity teacher = keycloakUserId == null ? null : teacherRepository.findByKeycloakUserId(keycloakUserId).orElse(null);

        String fullName = username;
        if (student != null){
            fullName = student.getStudentFullname();
        } else if (teacher != null){
            fullName = teacher.getTeacherFullname();
        }

        List<String> applicationRoles = roles.stream().filter(APPLICATION_ROLES::contains).toList();

        return new MeResponseDTO(
                email,
                username,
                fullName,
                applicationRoles,
                student == null ? null : student.getStudentId(),
                teacher == null ? null : teacher.getTeacherId()
        );
    }
}

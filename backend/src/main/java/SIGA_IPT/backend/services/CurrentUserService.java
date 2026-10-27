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

@Service
@Transactional(readOnly = true)
public class CurrentUserService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public CurrentUserService(StudentRepository studentRepository,
                              TeacherRepository teacherRepository){
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    public Long requireStudentId(String email){
        return studentRepository.findByStudentEmail(email == null ? "" : email)
                .map(StudentEntity::getStudentId)
                .orElseThrow(() -> new ForbiddenOperationException("No student record is linked to the authenticated user."));
    }

    public Long requireTeacherId(String email){
        return teacherRepository.findByTeacherEmail(email == null ? "" : email)
                .map(TeacherEntity::getTeacherId)
                .orElseThrow(() -> new ForbiddenOperationException("No teacher record is linked to the authenticated user."));
    }

    public MeResponseDTO getMe(String email, String username, List<String> roles){
        StudentEntity student = email == null ? null : studentRepository.findByStudentEmail(email).orElse(null);
        TeacherEntity teacher = email == null ? null : teacherRepository.findByTeacherEmail(email).orElse(null);

        String fullName = username;
        if (student != null){
            fullName = student.getStudentFullname();
        } else if (teacher != null){
            fullName = teacher.getTeacherFullname();
        }

        return new MeResponseDTO(
                email,
                username,
                fullName,
                roles,
                student == null ? null : student.getStudentId(),
                teacher == null ? null : teacher.getTeacherId()
        );
    }
}

package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.EnrollmentDTOs.EnrollmentResponseDTO;
import SIGA_IPT.backend.dtos.EnrollmentDTOs.GradeRequestDTO;
import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.EnrollmentEntity;
import SIGA_IPT.backend.entities.TeacherEntity;
import SIGA_IPT.backend.exceptions.ForbiddenOperationException;
import SIGA_IPT.backend.mappers.EnrollmentMapper;
import SIGA_IPT.backend.repositories.EnrollmentRepository;
import SIGA_IPT.backend.repositories.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GradeService {

    private static final double MIN_GRADE = 1.0;
    private static final double MAX_GRADE = 7.0;

    private final EnrollmentRepository enrollmentRepository;
    private final TeacherRepository teacherRepository;
    private final EnrollmentMapper enrollmentMapper;

    public GradeService(EnrollmentRepository enrollmentRepository,
                        TeacherRepository teacherRepository,
                        EnrollmentMapper enrollmentMapper){
        this.enrollmentRepository = enrollmentRepository;
        this.teacherRepository = teacherRepository;
        this.enrollmentMapper = enrollmentMapper;
    }

    private void validateFields(GradeRequestDTO dto){
        if (dto.finalGrade() == null){
            throw new IllegalArgumentException("The final grade is required.");
        }
        if (!(dto.finalGrade() >= MIN_GRADE && dto.finalGrade() <= MAX_GRADE)){
            throw new IllegalArgumentException("The final grade must be between " + MIN_GRADE + " and " + MAX_GRADE + ".");
        }
    }

    private EnrollmentEntity findEnrollmentOrThrow(Long id){
        return enrollmentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Enrollment not found: " + id));
    }

    private TeacherEntity findTeacherOrThrow(Long id){
        return teacherRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Teacher not found: " + id));
    }

    public EnrollmentResponseDTO registerGrade(Long enrollmentId, Long teacherId, GradeRequestDTO dto){
        validateFields(dto);

        EnrollmentEntity enrollment = findEnrollmentOrThrow(enrollmentId);
        TeacherEntity teacher = findTeacherOrThrow(teacherId);

        if (!enrollment.getSection().getTeacher().getTeacherId().equals(teacher.getTeacherId())){
            throw new ForbiddenOperationException("Only the teacher in charge of the section can register its final grades.");
        }
        if (enrollment.getAcademicPeriod().getAcademicPeriodStatus() != AcademicPeriodEntity.AcademicPeriodStatus.OPEN){
            throw new IllegalStateException("Cannot register the grade: the academic period is closed.");
        }

        enrollment.setFinalGrade(dto.finalGrade());

        return enrollmentMapper.toResponse(enrollmentRepository.save(enrollment));
    }
}

package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.StudentDTOs.StudentRequestDTO;
import SIGA_IPT.backend.dtos.StudentDTOs.StudentResponseDTO;
import SIGA_IPT.backend.dtos.StudentDTOs.StudentUpdateRequestDTO;
import SIGA_IPT.backend.entities.CareerEntity;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import SIGA_IPT.backend.mappers.StudentMapper;
import SIGA_IPT.backend.repositories.CareerRepository;
import SIGA_IPT.backend.repositories.EnrollmentRepository;
import SIGA_IPT.backend.repositories.StudentRepository;
import SIGA_IPT.backend.repositories.StudyplanRepository;
import SIGA_IPT.backend.repositories.TuitionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final CareerRepository careerRepository;
    private final StudyplanRepository studyplanRepository;
    private final TuitionRepository tuitionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentMapper studentMapper;

    public StudentService(StudentRepository studentRepository,
                          CareerRepository careerRepository,
                          StudyplanRepository studyplanRepository,
                          TuitionRepository tuitionRepository,
                          EnrollmentRepository enrollmentRepository,
                          StudentMapper studentMapper){
        this.studentRepository = studentRepository;
        this.careerRepository = careerRepository;
        this.studyplanRepository = studyplanRepository;
        this.tuitionRepository = tuitionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.studentMapper = studentMapper;
    }

    private void validateFields(StudentRequestDTO dto){
        if (dto.studentRun() == null || dto.studentRun().isBlank()){
            throw new IllegalArgumentException("The student RUN is required.");
        }
        if (dto.careerCode() == null || dto.careerCode().isBlank()){
            throw new IllegalArgumentException("The career code is required.");
        }
        validateProfile(dto.studentFullname(), dto.studentEmail());
    }

    private void validateUpdateFields(StudentUpdateRequestDTO dto){
        validateProfile(dto.studentFullname(), dto.studentEmail());
    }

    private void validateProfile(String fullname, String email){
        if (fullname == null || fullname.isBlank()){
            throw new IllegalArgumentException("The student full name is required.");
        }
        if (email == null || email.isBlank()){
            throw new IllegalArgumentException("The student email is required.");
        }
    }

    private StudentEntity findStudentOrThrow(Long id){
        return studentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Student not found: " + id));
    }

    public StudentResponseDTO create(StudentRequestDTO dto){
        validateFields(dto);

        if (studentRepository.existsByStudentRun(dto.studentRun())){
            throw new IllegalArgumentException("A student with this RUN already exists: " + dto.studentRun());
        }
        if (studentRepository.existsByStudentEmail(dto.studentEmail())){
            throw new IllegalArgumentException("A student with this email already exists: " + dto.studentEmail());
        }

        CareerEntity career = careerRepository.findByCareerCode(dto.careerCode())
                .orElseThrow(() -> new EntityNotFoundException("Career not found: " + dto.careerCode()));
        if (career.getCareerStatus() != CareerEntity.CareerStatus.ACTIVE){
            throw new IllegalStateException("Cannot register: the career is not active.");
        }

        StudyplanEntity currentPlan = studyplanRepository
                .findByCareerAndStudyplanStatus(career, StudyplanEntity.StudyplanStatus.CURRENT)
                .orElseThrow(() -> new IllegalStateException("Cannot register: the career has no current study plan."));

        StudentEntity student = studentMapper.toEntity(dto, career, currentPlan);
        student.setStudentStatus(StudentEntity.StudentStatus.REGULAR);

        return studentMapper.toResponse(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public List<StudentResponseDTO> getAll(){
        return studentMapper.toResponseList(studentRepository.findAll());
    }

    @Transactional(readOnly = true)
    public StudentResponseDTO getById(Long id){
        return studentMapper.toResponse(findStudentOrThrow(id));
    }

    public StudentResponseDTO update(Long id, StudentUpdateRequestDTO dto){
        validateUpdateFields(dto);
        StudentEntity student = findStudentOrThrow(id);

        boolean emailChanged = !student.getStudentEmail().equals(dto.studentEmail());
        if (emailChanged && studentRepository.existsByStudentEmail(dto.studentEmail())){
            throw new IllegalArgumentException("A student with this email already exists: " + dto.studentEmail());
        }

        studentMapper.updateEntity(student, dto);

        return studentMapper.toResponse(studentRepository.save(student));
    }

    public StudentResponseDTO changeStatus(Long id, StudentEntity.StudentStatus newStatus){
        if (newStatus == null){
            throw new IllegalArgumentException("The student status is required.");
        }
        if (newStatus == StudentEntity.StudentStatus.GRADUATED || newStatus == StudentEntity.StudentStatus.EXPELLED){
            throw new IllegalArgumentException("The status " + newStatus + " cannot be set manually.");
        }

        StudentEntity student = findStudentOrThrow(id);

        if (student.getStudentStatus() == StudentEntity.StudentStatus.GRADUATED
                || student.getStudentStatus() == StudentEntity.StudentStatus.EXPELLED){
            throw new IllegalStateException("Cannot change the status of a " + student.getStudentStatus() + " student.");
        }

        student.setStudentStatus(newStatus);

        return studentMapper.toResponse(studentRepository.save(student));
    }

    public void delete(Long id){
        StudentEntity student = findStudentOrThrow(id);

        if (tuitionRepository.existsByStudent(student)){
            throw new IllegalStateException("Cannot delete: the student has associated tuitions.");
        }
        if (enrollmentRepository.existsByStudent(student)){
            throw new IllegalStateException("Cannot delete: the student has associated enrollments.");
        }

        studentRepository.delete(student);
    }
}

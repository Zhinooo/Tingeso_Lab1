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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TuitionService {

    private final TuitionRepository tuitionRepository;
    private final StudentRepository studentRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TuitionMapper tuitionMapper;

    public TuitionService(TuitionRepository tuitionRepository,
                          StudentRepository studentRepository,
                          AcademicPeriodRepository academicPeriodRepository,
                          EnrollmentRepository enrollmentRepository,
                          TuitionMapper tuitionMapper){
        this.tuitionRepository = tuitionRepository;
        this.studentRepository = studentRepository;
        this.academicPeriodRepository = academicPeriodRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.tuitionMapper = tuitionMapper;
    }

    private void validateFields(TuitionRequestDTO dto){
        if (dto.studentId() == null){
            throw new IllegalArgumentException("The student is required.");
        }
        if (dto.academicPeriodId() == null){
            throw new IllegalArgumentException("The academic period is required.");
        }
    }

    private TuitionEntity findTuitionOrThrow(Long id){
        return tuitionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Tuition not found: " + id));
    }

    private StudentEntity findStudentOrThrow(Long id){
        return studentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Student not found: " + id));
    }

    private AcademicPeriodEntity findAcademicPeriodOrThrow(Long id){
        return academicPeriodRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Academic period not found: " + id));
    }

    public TuitionResponseDTO create(TuitionRequestDTO dto){
        validateFields(dto);

        StudentEntity student = findStudentOrThrow(dto.studentId());
        AcademicPeriodEntity academicPeriod = findAcademicPeriodOrThrow(dto.academicPeriodId());

        if (academicPeriod.getAcademicPeriodStatus() != AcademicPeriodEntity.AcademicPeriodStatus.OPEN){
            throw new IllegalStateException("Cannot register tuition: the academic period is closed.");
        }
        if (student.getStudentStatus() == StudentEntity.StudentStatus.GRADUATED
                || student.getStudentStatus() == StudentEntity.StudentStatus.EXPELLED){
            throw new IllegalStateException("Cannot register tuition: the student is " + student.getStudentStatus() + ".");
        }
        if (tuitionRepository.existsByStudentAndAcademicPeriod(student, academicPeriod)){
            throw new IllegalArgumentException("The student already has a tuition in this academic period.");
        }

        TuitionEntity tuition = tuitionMapper.toEntity(student, academicPeriod);
        tuition.setTuitionDate(LocalDate.now());

        return tuitionMapper.toResponse(tuitionRepository.save(tuition));
    }

    @Transactional(readOnly = true)
    public List<TuitionResponseDTO> getAll(){
        return tuitionMapper.toResponseList(tuitionRepository.findAll());
    }

    @Transactional(readOnly = true)
    public TuitionResponseDTO getById(Long id){
        return tuitionMapper.toResponse(findTuitionOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<TuitionResponseDTO> getByStudent(Long studentId){
        StudentEntity student = findStudentOrThrow(studentId);
        return tuitionMapper.toResponseList(tuitionRepository.findByStudent(student));
    }

    @Transactional(readOnly = true)
    public List<TuitionResponseDTO> getByAcademicPeriod(Long academicPeriodId){
        AcademicPeriodEntity academicPeriod = findAcademicPeriodOrThrow(academicPeriodId);
        return tuitionMapper.toResponseList(tuitionRepository.findByAcademicPeriod(academicPeriod));
    }

    public void delete(Long id){
        TuitionEntity tuition = findTuitionOrThrow(id);

        if (tuition.getAcademicPeriod().getAcademicPeriodStatus() != AcademicPeriodEntity.AcademicPeriodStatus.OPEN){
            throw new IllegalStateException("Cannot delete: the academic period is closed.");
        }
        if (enrollmentRepository.existsByStudentAndAcademicPeriod(tuition.getStudent(), tuition.getAcademicPeriod())){
            throw new IllegalStateException("Cannot delete: the student has enrollments in this academic period.");
        }

        tuitionRepository.delete(tuition);
    }






















}

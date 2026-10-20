package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.AcademicPeriodDTOs.AcademicPeriodRequestDTO;
import SIGA_IPT.backend.dtos.AcademicPeriodDTOs.AcademicPeriodResponseDTO;
import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.mappers.AcademicPeriodMapper;
import SIGA_IPT.backend.repositories.AcademicPeriodRepository;
import SIGA_IPT.backend.repositories.EnrollmentRepository;
import SIGA_IPT.backend.repositories.SectionRepository;
import SIGA_IPT.backend.repositories.TuitionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AcademicPeriodService {

    private final AcademicPeriodRepository academicPeriodRepository;
    private final SectionRepository sectionRepository;
    private final TuitionRepository tuitionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AcademicPeriodMapper academicPeriodMapper;

    public AcademicPeriodService(AcademicPeriodRepository academicPeriodRepository,
                                 SectionRepository sectionRepository,
                                 TuitionRepository tuitionRepository,
                                 EnrollmentRepository enrollmentRepository,
                                 AcademicPeriodMapper academicPeriodMapper){
        this.academicPeriodRepository = academicPeriodRepository;
        this.sectionRepository = sectionRepository;
        this.tuitionRepository = tuitionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.academicPeriodMapper = academicPeriodMapper;
    }

    private void validateFields(AcademicPeriodRequestDTO dto){
        if (dto.academicYear() == null){
            throw new IllegalArgumentException("The academic year is required.");
        }
        if (dto.academicSemester() == null){
            throw new IllegalArgumentException("The academic semester is required.");
        }
        if (dto.academicSemester() != 1 && dto.academicSemester() != 2){
            throw new IllegalArgumentException("The academic semester must be 1 or 2.");
        }
    }

    private AcademicPeriodEntity findAcademicPeriodOrThrow(Long id){
        return academicPeriodRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Academic period not found: " + id));
    }

    private boolean hasAssociations(AcademicPeriodEntity academicPeriod){
        return sectionRepository.existsByAcademicPeriod(academicPeriod)
                || tuitionRepository.existsByAcademicPeriod(academicPeriod)
                || enrollmentRepository.existsByAcademicPeriod(academicPeriod);
    }

    public AcademicPeriodResponseDTO create(AcademicPeriodRequestDTO dto){
        validateFields(dto);

        if (academicPeriodRepository.existsByAcademicYearAndAcademicSemester(dto.academicYear(), dto.academicSemester())){
            throw new IllegalArgumentException("This academic period already exists: " + dto.academicYear() + "-" + dto.academicSemester());
        }

        AcademicPeriodEntity academicPeriod = academicPeriodMapper.toEntity(dto);
        academicPeriod.setAcademicPeriodStatus(AcademicPeriodEntity.AcademicPeriodStatus.OPEN);

        return academicPeriodMapper.toResponse(academicPeriodRepository.save(academicPeriod));
    }

    public List<AcademicPeriodResponseDTO> getAll(){
        return academicPeriodMapper.toResponseList(academicPeriodRepository.findAll());
    }

    public AcademicPeriodResponseDTO getById(Long id){
        return academicPeriodMapper.toResponse(findAcademicPeriodOrThrow(id));
    }

    public AcademicPeriodResponseDTO update(Long id, AcademicPeriodRequestDTO dto){
        validateFields(dto);
        AcademicPeriodEntity academicPeriod = findAcademicPeriodOrThrow(id);

        if (academicPeriod.getAcademicPeriodStatus() == AcademicPeriodEntity.AcademicPeriodStatus.CLOSED){
            throw new IllegalStateException("Cannot modify: the academic period is closed.");
        }
        if (hasAssociations(academicPeriod)){
            throw new IllegalStateException("Cannot modify: the academic period has associated sections, tuitions or enrollments.");
        }

        boolean periodChanged = academicPeriod.getAcademicYear() != dto.academicYear()
                || academicPeriod.getAcademicSemester() != dto.academicSemester();

        if (periodChanged && academicPeriodRepository.existsByAcademicYearAndAcademicSemester(dto.academicYear(), dto.academicSemester())){
            throw new IllegalArgumentException("This academic period already exists: " + dto.academicYear() + "-" + dto.academicSemester());
        }

        academicPeriodMapper.updateEntity(academicPeriod, dto);

        return academicPeriodMapper.toResponse(academicPeriodRepository.save(academicPeriod));
    }

    public void delete(Long id){
        AcademicPeriodEntity academicPeriod = findAcademicPeriodOrThrow(id);

        if (hasAssociations(academicPeriod)){
            throw new IllegalStateException("Cannot delete: the academic period has associated sections, tuitions or enrollments.");
        }

        academicPeriodRepository.delete(academicPeriod);
    }
}

package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.StudyplanDTOs.StudyplanRequestDTO;
import SIGA_IPT.backend.dtos.StudyplanDTOs.StudyplanResponseDTO;
import SIGA_IPT.backend.entities.CareerEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import SIGA_IPT.backend.mappers.StudyplanMapper;
import SIGA_IPT.backend.repositories.CareerRepository;
import SIGA_IPT.backend.repositories.CourseRepository;
import SIGA_IPT.backend.repositories.StudentRepository;
import SIGA_IPT.backend.repositories.StudyplanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudyplanService {

    private final StudyplanRepository studyplanRepository;
    private final CareerRepository careerRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final StudyplanMapper studyplanMapper;

    public StudyplanService(StudyplanRepository studyplanRepository,
                            CareerRepository careerRepository,
                            StudentRepository studentRepository,
                            CourseRepository courseRepository,
                            StudyplanMapper studyplanMapper){
        this.studyplanRepository = studyplanRepository;
        this.careerRepository = careerRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.studyplanMapper = studyplanMapper;
    }

    public StudyplanResponseDTO create(StudyplanRequestDTO dto){
        validateIdentifier(dto);

        if(dto.careerCode() == null || dto.careerCode().isBlank()){
            throw new IllegalArgumentException("The career code is required.");
        }

        CareerEntity career = careerRepository.findByCareerCode(dto.careerCode()).orElseThrow(() -> new EntityNotFoundException("Career not found: " + dto.careerCode()));

        if (studyplanRepository.existsByStudyplanIdentifierAndCareer(dto.studyplanIdentifier(), career)){
            throw new IllegalArgumentException("This career already has a study plan with identifier: " + dto.studyplanIdentifier());
        }

        demoteCurrentPlan(career);

        StudyplanEntity studyplan = studyplanMapper.toEntity(dto, career);
        studyplan.setStudyplanStatus(StudyplanEntity.StudyplanStatus.CURRENT);

        return studyplanMapper.toResponse(studyplanRepository.save(studyplan));
    }

    @Transactional(readOnly = true)
    public List<StudyplanResponseDTO> getAll(){
        return studyplanMapper.toResponseList(studyplanRepository.findAll());
    }

    @Transactional(readOnly = true)
    public StudyplanResponseDTO getById(Long id){
        return studyplanMapper.toResponse(findStudyplanOrThrow(id));
    }

    public StudyplanResponseDTO update(Long id, StudyplanRequestDTO dto){
        validateIdentifier(dto);
        StudyplanEntity studyplan = findStudyplanOrThrow(id);
        CareerEntity career = studyplan.getCareer();

        if (dto.careerCode() != null && !dto.careerCode().equals(career.getCareerCode())){
            throw new IllegalArgumentException("A study plan cannot change its career.");
        }

        boolean identifierChanged = !studyplan.getStudyplanIdentifier().equals(dto.studyplanIdentifier());
        if (identifierChanged
                && studyplanRepository.existsByStudyplanIdentifierAndCareer(dto.studyplanIdentifier(), career)){
            throw new IllegalArgumentException("This career already has a study plan with identifier: " + dto.studyplanIdentifier());
        }

        studyplanMapper.updateEntity(studyplan, dto);

        return studyplanMapper.toResponse(studyplanRepository.save(studyplan));
    }

    public StudyplanResponseDTO changeStatus(Long id, StudyplanEntity.StudyplanStatus newStatus){
        StudyplanEntity studyplan = findStudyplanOrThrow(id);

        if (newStatus == StudyplanEntity.StudyplanStatus.CURRENT){
            demoteCurrentPlanExcept(studyplan);
        }
        studyplan.setStudyplanStatus(newStatus);

        return studyplanMapper.toResponse(studyplanRepository.save(studyplan));
    }

    public void delete(Long id){
        StudyplanEntity studyplan = findStudyplanOrThrow(id);

        if (studentRepository.existsByStudyplan(studyplan)){
            throw new IllegalStateException("Cannot delete: the study plan has associated students.");
        }
        if (courseRepository.existsByStudyplan(studyplan)){
            throw new IllegalStateException("Cannot delete: the study plan has associated courses.");
        }

        studyplanRepository.delete(studyplan);
    }

    private void demoteCurrentPlan(CareerEntity career){
        studyplanRepository
                .findByCareerAndStudyplanStatus(career, StudyplanEntity.StudyplanStatus.CURRENT)
                .ifPresent(current -> {
                    current.setStudyplanStatus(StudyplanEntity.StudyplanStatus.NOT_CURRENT);
                    studyplanRepository.save(current);
                });
    }

    private void demoteCurrentPlanExcept(StudyplanEntity keep){
        studyplanRepository
                .findByCareerAndStudyplanStatus(keep.getCareer(), StudyplanEntity.StudyplanStatus.CURRENT)
                .filter(current -> !current.getStudyplanId().equals(keep.getStudyplanId()))
                .ifPresent(current -> {
                    current.setStudyplanStatus(StudyplanEntity.StudyplanStatus.NOT_CURRENT);
                    studyplanRepository.save(current);
                });
    }

    private void validateIdentifier(StudyplanRequestDTO dto){
        if (dto.studyplanIdentifier() == null || dto.studyplanIdentifier().isBlank()){
            throw new IllegalArgumentException("The study plan identifier is required.");
        }
    }

    private StudyplanEntity findStudyplanOrThrow(Long id){
        return studyplanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Study plan not found: " + id));
    }
}

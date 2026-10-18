package SIGA_IPT.backend.mappers;

import SIGA_IPT.backend.dtos.StudyplanDTOs.StudyplanRequestDTO;
import SIGA_IPT.backend.dtos.StudyplanDTOs.StudyplanResponseDTO;
import SIGA_IPT.backend.entities.CareerEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudyplanMapper {

    public StudyplanEntity toEntity(StudyplanRequestDTO dto, CareerEntity career) {
        StudyplanEntity studyplan = new StudyplanEntity();
        studyplan.setStudyplanIdentifier(dto.studyplanIdentifier());
        studyplan.setCareer(career);
        return studyplan;
    }

    public void updateEntity(StudyplanEntity studyplan, StudyplanRequestDTO dto) {
        studyplan.setStudyplanIdentifier(dto.studyplanIdentifier());
    }

    public StudyplanResponseDTO toResponse(StudyplanEntity studyplan) {
        return new StudyplanResponseDTO(
                studyplan.getStudyplanId(),
                studyplan.getStudyplanIdentifier(),
                studyplan.getCareer().getCareerCode(),
                studyplan.getStudyplanStatus()
        );
    }

    public List<StudyplanResponseDTO> toResponseList(List<StudyplanEntity> studyplans) {
        return studyplans.stream().map(this::toResponse).toList();
    }
}

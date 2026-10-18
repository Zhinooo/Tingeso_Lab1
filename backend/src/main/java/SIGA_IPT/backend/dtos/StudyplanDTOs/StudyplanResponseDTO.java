package SIGA_IPT.backend.dtos.StudyplanDTOs;

import SIGA_IPT.backend.entities.StudyplanEntity;

public record StudyplanResponseDTO(
        Long studyplanId,
        String studyplanIdentifier,
        String careerCode,
        StudyplanEntity.StudyplanStatus studyplanStatus
) {
}

package SIGA_IPT.backend.dtos.StudyplanDTOs;

public record StudyplanRequestDTO(
        String studyplanIdentifier,
        String careerCode,
        Boolean current
) {
}

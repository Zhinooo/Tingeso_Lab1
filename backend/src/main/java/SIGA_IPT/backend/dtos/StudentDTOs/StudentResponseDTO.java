package SIGA_IPT.backend.dtos.StudentDTOs;

import SIGA_IPT.backend.entities.StudentEntity;

public record StudentResponseDTO(
        Long studentId,
        String studentRun,
        String studentFullname,
        String studentEmail,
        String careerCode,
        String studyplanIdentifier,
        StudentEntity.StudentStatus studentStatus
) {
}

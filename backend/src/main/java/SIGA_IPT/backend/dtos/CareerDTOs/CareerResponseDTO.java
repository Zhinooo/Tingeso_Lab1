package SIGA_IPT.backend.dtos.CareerDTOs;

import SIGA_IPT.backend.entities.CareerEntity;

public record CareerResponseDTO(
        Long careerId,
        String careerCode,
        String careerName,
        String careerDescription,
        CareerEntity.CareerStatus careerStatus
) {
}

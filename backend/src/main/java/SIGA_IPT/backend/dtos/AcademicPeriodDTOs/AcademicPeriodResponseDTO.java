package SIGA_IPT.backend.dtos.AcademicPeriodDTOs;

import SIGA_IPT.backend.entities.AcademicPeriodEntity;

public record AcademicPeriodResponseDTO(
        Long academicPeriodId,
        int academicYear,
        int academicSemester,
        AcademicPeriodEntity.AcademicPeriodStatus academicPeriodStatus
) {
}

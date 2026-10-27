package SIGA_IPT.backend.dtos.EnrollmentDTOs;

import java.util.List;

public record EnrollmentRequestDTO(
        Long academicPeriodId,
        List<Long> courseIds
) {
}

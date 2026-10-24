package SIGA_IPT.backend.dtos.TuitionDTOs;

import java.time.LocalDate;

public record TuitionResponseDTO(
        Long tuitionId,
        Long studentId,
        String studentRun,
        Long academicPeriodId,
        int academicYear,
        int academicSemester,
        LocalDate tuitionDate
) {
}

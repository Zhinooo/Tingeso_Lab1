package SIGA_IPT.backend.dtos.EnrollmentDTOs;

import java.time.LocalDate;

public record EnrollmentResponseDTO(
        Long enrollmentId,
        Long studentId,
        String studentRun,
        Long courseId,
        String courseCode,
        String courseName,
        Long sectionId,
        Long academicPeriodId,
        int academicYear,
        int academicSemester,
        LocalDate enrollmentDate,
        Double finalGrade
) {
}

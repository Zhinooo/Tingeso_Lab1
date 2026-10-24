package SIGA_IPT.backend.dtos.SectionDTOs;

import java.util.List;

public record SectionResponseDTO(
        Long sectionId,
        Long courseId,
        String courseCode,
        String courseName,
        Long academicPeriodId,
        int academicYear,
        int academicSemester,
        Long teacherId,
        String teacherFullname,
        int sectionCapacity,
        List<String> scheduleBlocks
) {
}

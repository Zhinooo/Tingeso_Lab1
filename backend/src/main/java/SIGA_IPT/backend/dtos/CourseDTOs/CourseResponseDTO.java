package SIGA_IPT.backend.dtos.CourseDTOs;

import java.util.List;

public record CourseResponseDTO(
        Long courseId,
        String courseCode,
        String courseName,
        int semester,
        int theoryHours,
        int exerciseHours,
        int labHours,
        int sct,
        Long studyplanId,
        String studyplanIdentifier,
        List<String> prerequisiteCodes
) {
}

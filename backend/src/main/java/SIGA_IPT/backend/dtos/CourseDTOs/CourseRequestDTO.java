package SIGA_IPT.backend.dtos.CourseDTOs;

import java.util.List;

public record CourseRequestDTO(
        String courseCode,
        String courseName,
        Integer semester,
        Integer theoryHours,
        Integer exerciseHours,
        Integer labHours,
        Integer sct,
        Long studyplanId,
        List<String> prerequisiteCodes
) {
}

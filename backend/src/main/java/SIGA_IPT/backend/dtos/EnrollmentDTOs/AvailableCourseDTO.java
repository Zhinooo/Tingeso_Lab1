package SIGA_IPT.backend.dtos.EnrollmentDTOs;

import java.util.List;

public record AvailableCourseDTO(
        Long courseId,
        String courseCode,
        String courseName,
        int semester,
        Long sectionId,
        String teacherFullname,
        List<String> scheduleBlocks,
        int sectionCapacity,
        long availableSeats
) {
}

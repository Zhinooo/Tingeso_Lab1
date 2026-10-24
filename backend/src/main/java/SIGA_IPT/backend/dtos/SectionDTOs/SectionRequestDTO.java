package SIGA_IPT.backend.dtos.SectionDTOs;

import java.util.List;

public record SectionRequestDTO(
        Long courseId,
        Long academicPeriodId,
        Long teacherId,
        Integer sectionCapacity,
        List<ScheduleBlockDTO> scheduleBlocks
) {
}

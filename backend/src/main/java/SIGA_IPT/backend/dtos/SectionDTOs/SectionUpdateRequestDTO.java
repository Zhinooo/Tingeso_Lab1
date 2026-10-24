package SIGA_IPT.backend.dtos.SectionDTOs;

import java.util.List;

public record SectionUpdateRequestDTO(
        Long teacherId,
        Integer sectionCapacity,
        List<ScheduleBlockDTO> scheduleBlocks
) {
}

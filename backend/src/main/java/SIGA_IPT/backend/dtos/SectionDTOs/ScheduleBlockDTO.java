package SIGA_IPT.backend.dtos.SectionDTOs;

import SIGA_IPT.backend.entities.SectionEntity;

public record ScheduleBlockDTO(
        SectionEntity.ScheduleDay scheduleDay,
        Integer module
) {
}

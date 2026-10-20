package SIGA_IPT.backend.dtos.TeacherDTOs;

import SIGA_IPT.backend.entities.TeacherEntity;

public record TeacherResponseDTO(
        Long teacherId,
        String teacherRun,
        String teacherFullname,
        String teacherEmail,
        String teacherProtitle,
        String teacherDegree,
        TeacherEntity.TeacherStatus teacherStatus
) {
}

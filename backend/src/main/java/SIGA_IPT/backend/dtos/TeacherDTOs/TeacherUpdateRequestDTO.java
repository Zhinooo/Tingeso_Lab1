package SIGA_IPT.backend.dtos.TeacherDTOs;

public record TeacherUpdateRequestDTO(
        String teacherFullname,
        String teacherEmail,
        String teacherProtitle,
        String teacherDegree
) {
}

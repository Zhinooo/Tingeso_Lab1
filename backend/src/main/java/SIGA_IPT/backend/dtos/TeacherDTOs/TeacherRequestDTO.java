package SIGA_IPT.backend.dtos.TeacherDTOs;

public record TeacherRequestDTO(
        String teacherRun,
        String teacherFullname,
        String teacherEmail,
        String teacherProtitle,
        String teacherDegree
) {
}

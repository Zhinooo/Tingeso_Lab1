package SIGA_IPT.backend.dtos.StudentDTOs;

public record StudentRequestDTO(
        String studentRun,
        String studentFullname,
        String studentEmail,
        String careerCode,
        String password
) {
}

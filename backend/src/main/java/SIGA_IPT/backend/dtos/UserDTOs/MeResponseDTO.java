package SIGA_IPT.backend.dtos.UserDTOs;

import java.util.List;

public record MeResponseDTO(
        String email,
        String username,
        String fullName,
        List<String> roles,
        Long studentId,
        Long teacherId
) {
}

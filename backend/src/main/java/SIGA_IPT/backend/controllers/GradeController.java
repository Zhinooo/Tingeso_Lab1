package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.dtos.EnrollmentDTOs.EnrollmentResponseDTO;
import SIGA_IPT.backend.dtos.EnrollmentDTOs.GradeRequestDTO;
import SIGA_IPT.backend.services.CurrentUserService;
import SIGA_IPT.backend.services.GradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
public class GradeController {

    private final GradeService gradeService;
    private final CurrentUserService currentUserService;

    public GradeController(GradeService gradeService,
                           CurrentUserService currentUserService){
        this.gradeService = gradeService;
        this.currentUserService = currentUserService;
    }

    @PutMapping("/{enrollmentId}/grade")
    public ResponseEntity<EnrollmentResponseDTO> registerGrade(@AuthenticationPrincipal Jwt jwt, @PathVariable Long enrollmentId, @RequestBody GradeRequestDTO dto){
        Long teacherId = currentUserService.requireTeacherId(jwt.getClaimAsString("email"));
        return ResponseEntity.ok(gradeService.registerGrade(enrollmentId, teacherId, dto));
    }
}

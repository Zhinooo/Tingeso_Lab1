package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.dtos.EnrollmentDTOs.EnrollmentResponseDTO;
import SIGA_IPT.backend.dtos.EnrollmentDTOs.GradeRequestDTO;
import SIGA_IPT.backend.services.GradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService){
        this.gradeService = gradeService;
    }

    @PutMapping("/{enrollmentId}/grade")
    public ResponseEntity<EnrollmentResponseDTO> registerGrade(@PathVariable Long enrollmentId, @RequestParam Long teacherId, @RequestBody GradeRequestDTO dto){
        return ResponseEntity.ok(gradeService.registerGrade(enrollmentId, teacherId, dto));
    }
}

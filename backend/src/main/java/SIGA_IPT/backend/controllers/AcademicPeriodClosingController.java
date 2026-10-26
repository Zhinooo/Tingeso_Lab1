package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.dtos.AcademicPeriodDTOs.AcademicPeriodResponseDTO;
import SIGA_IPT.backend.services.AcademicPeriodClosingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/academic-periods")
public class AcademicPeriodClosingController {

    private final AcademicPeriodClosingService academicPeriodClosingService;

    public AcademicPeriodClosingController(AcademicPeriodClosingService academicPeriodClosingService){
        this.academicPeriodClosingService = academicPeriodClosingService;
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<AcademicPeriodResponseDTO> close(@PathVariable Long id){
        return ResponseEntity.ok(academicPeriodClosingService.close(id));
    }
}

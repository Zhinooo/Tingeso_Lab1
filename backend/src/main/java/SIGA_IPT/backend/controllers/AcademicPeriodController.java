package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.dtos.AcademicPeriodDTOs.AcademicPeriodRequestDTO;
import SIGA_IPT.backend.dtos.AcademicPeriodDTOs.AcademicPeriodResponseDTO;
import SIGA_IPT.backend.services.AcademicPeriodService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/academic-periods")
public class AcademicPeriodController {

    private final AcademicPeriodService academicPeriodService;

    public AcademicPeriodController(AcademicPeriodService academicPeriodService){
        this.academicPeriodService = academicPeriodService;
    }

    @PostMapping
    public ResponseEntity<AcademicPeriodResponseDTO> create(@RequestBody AcademicPeriodRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(academicPeriodService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<AcademicPeriodResponseDTO>> getAll(){
        return ResponseEntity.ok(academicPeriodService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcademicPeriodResponseDTO> getById(@PathVariable Long id){
        return ResponseEntity.ok(academicPeriodService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AcademicPeriodResponseDTO> update(@PathVariable Long id, @RequestBody AcademicPeriodRequestDTO dto){
        return ResponseEntity.ok(academicPeriodService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        academicPeriodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

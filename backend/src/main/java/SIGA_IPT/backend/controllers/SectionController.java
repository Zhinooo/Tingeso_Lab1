package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.dtos.SectionDTOs.SectionRequestDTO;
import SIGA_IPT.backend.dtos.SectionDTOs.SectionResponseDTO;
import SIGA_IPT.backend.dtos.SectionDTOs.SectionUpdateRequestDTO;
import SIGA_IPT.backend.services.CurrentUserService;
import SIGA_IPT.backend.services.SectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
@RequestMapping("/api/sections")
public class SectionController {

    private final SectionService sectionService;
    private final CurrentUserService currentUserService;

    public SectionController(SectionService sectionService,
                             CurrentUserService currentUserService){
        this.sectionService = sectionService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<SectionResponseDTO> create(@RequestBody SectionRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(sectionService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<SectionResponseDTO>> getAll(){
        return ResponseEntity.ok(sectionService.getAll());
    }

    @GetMapping("/me")
    public ResponseEntity<List<SectionResponseDTO>> getMine(@AuthenticationPrincipal Jwt jwt){
        Long teacherId = currentUserService.requireTeacherId(jwt.getClaimAsString("email"));
        return ResponseEntity.ok(sectionService.getByTeacher(teacherId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectionResponseDTO> getById(@PathVariable Long id){
        return ResponseEntity.ok(sectionService.getById(id));
    }

    @GetMapping("/academic-period/{academicPeriodId}")
    public ResponseEntity<List<SectionResponseDTO>> getByAcademicPeriod(@PathVariable Long academicPeriodId){
        return ResponseEntity.ok(sectionService.getByAcademicPeriod(academicPeriodId));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<SectionResponseDTO>> getByTeacher(@PathVariable Long teacherId){
        return ResponseEntity.ok(sectionService.getByTeacher(teacherId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SectionResponseDTO> update(@PathVariable Long id, @RequestBody SectionUpdateRequestDTO dto){
        return ResponseEntity.ok(sectionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        sectionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

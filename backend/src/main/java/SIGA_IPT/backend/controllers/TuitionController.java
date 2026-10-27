package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.dtos.TuitionDTOs.TuitionRequestDTO;
import SIGA_IPT.backend.dtos.TuitionDTOs.TuitionResponseDTO;
import SIGA_IPT.backend.services.CurrentUserService;
import SIGA_IPT.backend.services.TuitionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tuitions")
public class TuitionController {

    private final TuitionService tuitionService;
    private final CurrentUserService currentUserService;

    public TuitionController(TuitionService tuitionService,
                             CurrentUserService currentUserService){
        this.tuitionService = tuitionService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<TuitionResponseDTO> create(@RequestBody TuitionRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(tuitionService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<TuitionResponseDTO>> getAll(){
        return ResponseEntity.ok(tuitionService.getAll());
    }

    @GetMapping("/me")
    public ResponseEntity<List<TuitionResponseDTO>> getMine(@AuthenticationPrincipal Jwt jwt){
        return ResponseEntity.ok(tuitionService.getByStudent(currentUserService.requireStudentId(jwt.getClaimAsString("email"))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TuitionResponseDTO> getById(@PathVariable Long id){
        return ResponseEntity.ok(tuitionService.getById(id));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<TuitionResponseDTO>> getByStudent(@PathVariable Long studentId){
        return ResponseEntity.ok(tuitionService.getByStudent(studentId));
    }

    @GetMapping("/academic-period/{academicPeriodId}")
    public ResponseEntity<List<TuitionResponseDTO>> getByAcademicPeriod(@PathVariable Long academicPeriodId){
        return ResponseEntity.ok(tuitionService.getByAcademicPeriod(academicPeriodId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        tuitionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

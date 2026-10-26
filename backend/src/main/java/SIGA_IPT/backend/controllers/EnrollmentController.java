package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.dtos.EnrollmentDTOs.AvailableCourseDTO;
import SIGA_IPT.backend.dtos.EnrollmentDTOs.EnrollmentRequestDTO;
import SIGA_IPT.backend.dtos.EnrollmentDTOs.EnrollmentResponseDTO;
import SIGA_IPT.backend.services.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService){
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<List<EnrollmentResponseDTO>> create(@RequestBody EnrollmentRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<EnrollmentResponseDTO>> getAll(){
        return ResponseEntity.ok(enrollmentService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentResponseDTO> getById(@PathVariable Long id){
        return ResponseEntity.ok(enrollmentService.getById(id));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrollmentResponseDTO>> getByStudent(@PathVariable Long studentId){
        return ResponseEntity.ok(enrollmentService.getByStudent(studentId));
    }

    @GetMapping("/student/{studentId}/history")
    public ResponseEntity<List<EnrollmentResponseDTO>> getAcademicHistory(@PathVariable Long studentId){
        return ResponseEntity.ok(enrollmentService.getAcademicHistory(studentId));
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableCourseDTO>> getAvailableOffer(@RequestParam Long studentId, @RequestParam Long academicPeriodId){
        return ResponseEntity.ok(enrollmentService.getAvailableOffer(studentId, academicPeriodId));
    }

    @GetMapping("/academic-period/{academicPeriodId}")
    public ResponseEntity<List<EnrollmentResponseDTO>> getByAcademicPeriod(@PathVariable Long academicPeriodId){
        return ResponseEntity.ok(enrollmentService.getByAcademicPeriod(academicPeriodId));
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<EnrollmentResponseDTO>> getBySection(@PathVariable Long sectionId){
        return ResponseEntity.ok(enrollmentService.getBySection(sectionId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id){
        enrollmentService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}

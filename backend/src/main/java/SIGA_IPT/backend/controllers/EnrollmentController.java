package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.dtos.EnrollmentDTOs.AvailableCourseDTO;
import SIGA_IPT.backend.dtos.EnrollmentDTOs.EnrollmentRequestDTO;
import SIGA_IPT.backend.dtos.EnrollmentDTOs.EnrollmentResponseDTO;
import SIGA_IPT.backend.services.CurrentUserService;
import SIGA_IPT.backend.services.EnrollmentService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CurrentUserService currentUserService;

    public EnrollmentController(EnrollmentService enrollmentService,
                                CurrentUserService currentUserService){
        this.enrollmentService = enrollmentService;
        this.currentUserService = currentUserService;
    }

    private Long studentIdOf(Jwt jwt){
        return currentUserService.requireStudentId(jwt.getSubject());
    }

    private Long teacherIdOf(Jwt jwt){
        return currentUserService.requireTeacherId(jwt.getSubject());
    }

    @PostMapping
    public ResponseEntity<List<EnrollmentResponseDTO>> create(@AuthenticationPrincipal Jwt jwt, @RequestBody EnrollmentRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.create(studentIdOf(jwt), dto));
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

    @GetMapping("/academic-period/{academicPeriodId}")
    public ResponseEntity<List<EnrollmentResponseDTO>> getByAcademicPeriod(@PathVariable Long academicPeriodId){
        return ResponseEntity.ok(enrollmentService.getByAcademicPeriod(academicPeriodId));
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<EnrollmentResponseDTO>> getBySection(@PathVariable Long sectionId){
        return ResponseEntity.ok(enrollmentService.getBySection(sectionId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<EnrollmentResponseDTO>> getMine(@AuthenticationPrincipal Jwt jwt){
        return ResponseEntity.ok(enrollmentService.getByStudent(studentIdOf(jwt)));
    }

    @GetMapping("/me/history")
    public ResponseEntity<List<EnrollmentResponseDTO>> getMyAcademicHistory(@AuthenticationPrincipal Jwt jwt){
        return ResponseEntity.ok(enrollmentService.getAcademicHistory(studentIdOf(jwt)));
    }

    @GetMapping("/me/available")
    public ResponseEntity<List<AvailableCourseDTO>> getMyAvailableOffer(@AuthenticationPrincipal Jwt jwt, @RequestParam Long academicPeriodId){
        return ResponseEntity.ok(enrollmentService.getAvailableOffer(studentIdOf(jwt), academicPeriodId));
    }

    @GetMapping("/me/section/{sectionId}")
    public ResponseEntity<List<EnrollmentResponseDTO>> getMySectionEnrollments(@AuthenticationPrincipal Jwt jwt, @PathVariable Long sectionId){
        return ResponseEntity.ok(enrollmentService.getBySectionForTeacher(sectionId, teacherIdOf(jwt)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id){
        enrollmentService.cancel(id, studentIdOf(jwt));
        return ResponseEntity.noContent().build();
    }
}

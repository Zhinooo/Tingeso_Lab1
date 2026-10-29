package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.dtos.StudentDTOs.StudentCareerChangeRequestDTO;
import SIGA_IPT.backend.dtos.StudentDTOs.StudentRequestDTO;
import SIGA_IPT.backend.dtos.StudentDTOs.StudentResponseDTO;
import SIGA_IPT.backend.dtos.StudentDTOs.StudentUpdateRequestDTO;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.services.CurrentUserService;
import SIGA_IPT.backend.services.StudentService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final CurrentUserService currentUserService;

    public StudentController(StudentService studentService,
                             CurrentUserService currentUserService){
        this.studentService = studentService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<StudentResponseDTO> create(@RequestBody StudentRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<StudentResponseDTO>> getAll(){
        return ResponseEntity.ok(studentService.getAll());
    }

    @GetMapping("/me")
    public ResponseEntity<StudentResponseDTO> getMine(@AuthenticationPrincipal Jwt jwt){
        return ResponseEntity.ok(studentService.getById(currentUserService.requireStudentId(jwt.getSubject())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> getById(@PathVariable Long id){
        return ResponseEntity.ok(studentService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> update(@PathVariable Long id, @RequestBody StudentUpdateRequestDTO dto){
        return ResponseEntity.ok(studentService.update(id, dto));
    }

    @PutMapping("/{id}/career")
    public ResponseEntity<StudentResponseDTO> changeCareerAndPlan(@PathVariable Long id, @RequestBody StudentCareerChangeRequestDTO dto){
        return ResponseEntity.ok(studentService.changeCareerAndPlan(id, dto));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<StudentResponseDTO> changeStatus(@PathVariable Long id, @RequestParam StudentEntity.StudentStatus status){
        return ResponseEntity.ok(studentService.changeStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

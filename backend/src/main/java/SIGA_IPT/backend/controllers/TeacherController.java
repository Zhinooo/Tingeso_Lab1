package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.dtos.TeacherDTOs.TeacherRequestDTO;
import SIGA_IPT.backend.dtos.TeacherDTOs.TeacherResponseDTO;
import SIGA_IPT.backend.dtos.TeacherDTOs.TeacherUpdateRequestDTO;
import SIGA_IPT.backend.entities.TeacherEntity;
import SIGA_IPT.backend.services.TeacherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService){
        this.teacherService = teacherService;
    }

    @PostMapping
    public ResponseEntity<TeacherResponseDTO> create(@RequestBody TeacherRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(teacherService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<TeacherResponseDTO>> getAll(){
        return ResponseEntity.ok(teacherService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherResponseDTO> getById(@PathVariable Long id){
        return ResponseEntity.ok(teacherService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponseDTO> update(@PathVariable Long id, @RequestBody TeacherUpdateRequestDTO dto){
        return ResponseEntity.ok(teacherService.update(id, dto));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TeacherResponseDTO> changeStatus(@PathVariable Long id, @RequestParam TeacherEntity.TeacherStatus status){
        return ResponseEntity.ok(teacherService.changeStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        teacherService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

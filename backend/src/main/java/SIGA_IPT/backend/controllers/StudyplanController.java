package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.dtos.StudyplanDTOs.StudyplanRequestDTO;
import SIGA_IPT.backend.dtos.StudyplanDTOs.StudyplanResponseDTO;
import SIGA_IPT.backend.entities.StudyplanEntity;
import SIGA_IPT.backend.services.StudyplanService;
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
@RequestMapping("/api/studyplans")
public class StudyplanController {

    private final StudyplanService studyplanService;

    public StudyplanController(StudyplanService studyplanService){
        this.studyplanService = studyplanService;
    }

    @PostMapping
    public ResponseEntity<StudyplanResponseDTO> create(@RequestBody StudyplanRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(studyplanService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<StudyplanResponseDTO>> getAll(){
        return ResponseEntity.ok(studyplanService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyplanResponseDTO> getById(@PathVariable Long id){
        return ResponseEntity.ok(studyplanService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyplanResponseDTO> update(@PathVariable Long id, @RequestBody StudyplanRequestDTO dto){
        return ResponseEntity.ok(studyplanService.update(id, dto));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<StudyplanResponseDTO> changeStatus(@PathVariable Long id, @RequestParam StudyplanEntity.StudyplanStatus status){
        return ResponseEntity.ok(studyplanService.changeStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        studyplanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

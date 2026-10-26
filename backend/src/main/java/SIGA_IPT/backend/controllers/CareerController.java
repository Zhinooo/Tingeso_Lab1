package SIGA_IPT.backend.controllers;

import SIGA_IPT.backend.dtos.CareerDTOs.CareerRequestDTO;
import SIGA_IPT.backend.dtos.CareerDTOs.CareerResponseDTO;
import SIGA_IPT.backend.entities.CareerEntity;
import SIGA_IPT.backend.services.CareerService;
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
@RequestMapping("/api/careers")
public class CareerController {

    private final CareerService careerService;

    public CareerController(CareerService careerService){
        this.careerService = careerService;
    }

    @PostMapping
    public ResponseEntity<CareerResponseDTO> create(@RequestBody CareerRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(careerService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<CareerResponseDTO>> getAll(){
        return ResponseEntity.ok(careerService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CareerResponseDTO> getById(@PathVariable Long id){
        return ResponseEntity.ok(careerService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CareerResponseDTO> update(@PathVariable Long id, @RequestBody CareerRequestDTO dto){
        return ResponseEntity.ok(careerService.update(id, dto));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CareerResponseDTO> changeStatus(@PathVariable Long id, @RequestParam CareerEntity.CareerStatus status){
        return ResponseEntity.ok(careerService.changeStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        careerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.CareerDTOs.CareerRequestDTO;
import SIGA_IPT.backend.dtos.CareerDTOs.CareerResponseDTO;
import SIGA_IPT.backend.entities.CareerEntity;
import SIGA_IPT.backend.mappers.CareerMapper;
import SIGA_IPT.backend.repositories.CareerRepository;
import SIGA_IPT.backend.repositories.StudentRepository;
import SIGA_IPT.backend.repositories.StudyplanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CareerService {

    private final CareerRepository careerRepository;
    private final StudentRepository studentRepository;
    private final StudyplanRepository studyplanRepository;
    private final CareerMapper careerMapper;

    public CareerService(CareerRepository careerRepository,
                         StudentRepository studentRepository,
                         StudyplanRepository studyplanRepository,
                         CareerMapper careerMapper){
        this.careerRepository = careerRepository;
        this.studentRepository = studentRepository;
        this.studyplanRepository = studyplanRepository;
        this.careerMapper = careerMapper;
    }

    public CareerResponseDTO create(CareerRequestDTO dto){
        validateFields(dto);

        if(careerRepository.existsByCareerCode(dto.careerCode())){
            throw new IllegalArgumentException("A career with this code already exists: " + dto.careerCode());
        }

        CareerEntity career = careerMapper.toEntity(dto);
        career.setCareerStatus(CareerEntity.CareerStatus.ACTIVE);

        return careerMapper.toResponse(careerRepository.save(career));
    }

    public List<CareerResponseDTO> getAll(){
        return careerMapper.toResponseList(careerRepository.findAll());
    }

    public CareerResponseDTO getById(Long id){
        return careerMapper.toResponse(findCareerOrThrow(id));
    }

    public CareerResponseDTO update(Long id, CareerRequestDTO dto){
        validateFields(dto);
        CareerEntity career = findCareerOrThrow(id);

        boolean codeChanged = !career.getCareerCode().equals(dto.careerCode());
        if (codeChanged && careerRepository.existsByCareerCode(dto.careerCode())){
            throw new IllegalArgumentException("A career with this code already exists: " + dto.careerCode());
        }

        careerMapper.updateEntity(career, dto);

        return careerMapper.toResponse(careerRepository.save(career));
    }

    public CareerResponseDTO changeStatus(Long id, CareerEntity.CareerStatus newStatus){
        CareerEntity career = findCareerOrThrow(id);
        career.setCareerStatus(newStatus);
        return careerMapper.toResponse(careerRepository.save(career));
    }

    public void delete(Long id){
        CareerEntity career = findCareerOrThrow(id);

        if (studentRepository.existsByCareer(career)){
            throw new IllegalStateException("Cannot delete: the career has associated students.");
        }
        if (studyplanRepository.existsByCareer(career)){
            throw new IllegalStateException("Cannot delete: the career has associated study plans.");
        }

        careerRepository.delete(career);
    }

    private void validateFields(CareerRequestDTO dto){
        if (dto.careerCode() == null || dto.careerCode().isBlank()){
            throw new IllegalArgumentException("The career code is required.");
        }
        if (dto.careerName() == null || dto.careerName().isBlank()){
            throw new IllegalArgumentException("The career name is required.");
        }
        if (dto.careerDescription() == null || dto.careerDescription().isBlank()){
            throw new IllegalArgumentException("The career description is required.");
        }
    }

    private CareerEntity findCareerOrThrow(Long id){
        return careerRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Career not found: " + id));
    }
}

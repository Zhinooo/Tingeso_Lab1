package SIGA_IPT.backend.mappers;

import SIGA_IPT.backend.dtos.CareerDTOs.CareerRequestDTO;
import SIGA_IPT.backend.dtos.CareerDTOs.CareerResponseDTO;
import SIGA_IPT.backend.entities.CareerEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CareerMapper {

    public CareerEntity toEntity(CareerRequestDTO dto) {
        CareerEntity career = new CareerEntity();
        updateEntity(career, dto);
        return career;
    }

    public void updateEntity(CareerEntity career, CareerRequestDTO dto) {
        career.setCareerCode(dto.careerCode());
        career.setCareerName(dto.careerName());
        career.setCareerDescription(dto.careerDescription());
    }

    public CareerResponseDTO toResponse(CareerEntity career) {
        return new CareerResponseDTO(
                career.getCareerId(),
                career.getCareerCode(),
                career.getCareerName(),
                career.getCareerDescription(),
                career.getCareerStatus()
        );
    }

    public List<CareerResponseDTO> toResponseList(List<CareerEntity> careers) {
        return careers.stream().map(this::toResponse).toList();
    }
}

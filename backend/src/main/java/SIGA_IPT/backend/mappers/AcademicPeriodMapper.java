package SIGA_IPT.backend.mappers;

import SIGA_IPT.backend.dtos.AcademicPeriodDTOs.AcademicPeriodRequestDTO;
import SIGA_IPT.backend.dtos.AcademicPeriodDTOs.AcademicPeriodResponseDTO;
import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AcademicPeriodMapper {

    public AcademicPeriodEntity toEntity(AcademicPeriodRequestDTO dto){
        AcademicPeriodEntity academicPeriod = new AcademicPeriodEntity();
        updateEntity(academicPeriod, dto);
        return academicPeriod;
    }

    public void updateEntity(AcademicPeriodEntity academicPeriod, AcademicPeriodRequestDTO dto){
        academicPeriod.setAcademicYear(dto.academicYear());
        academicPeriod.setAcademicSemester(dto.academicSemester());
    }

    public AcademicPeriodResponseDTO toResponse(AcademicPeriodEntity academicPeriod){
        return new AcademicPeriodResponseDTO(
                academicPeriod.getAcademicPeriodId(),
                academicPeriod.getAcademicYear(),
                academicPeriod.getAcademicSemester(),
                academicPeriod.getAcademicPeriodStatus()
        );
    }

    public List<AcademicPeriodResponseDTO> toResponseList(List<AcademicPeriodEntity> academicPeriods){
        return academicPeriods.stream().map(this::toResponse).toList();
    }
}

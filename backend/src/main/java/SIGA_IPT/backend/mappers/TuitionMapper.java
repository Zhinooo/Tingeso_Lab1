package SIGA_IPT.backend.mappers;

import SIGA_IPT.backend.dtos.TuitionDTOs.TuitionResponseDTO;
import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.entities.TuitionEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TuitionMapper {

    public TuitionEntity toEntity(StudentEntity student, AcademicPeriodEntity academicPeriod){
        TuitionEntity tuition = new TuitionEntity();
        tuition.setStudent(student);
        tuition.setAcademicPeriod(academicPeriod);
        return tuition;
    }

    public TuitionResponseDTO toResponse(TuitionEntity tuition){
        return new TuitionResponseDTO(
                tuition.getTuitionId(),
                tuition.getStudent().getStudentId(),
                tuition.getStudent().getStudentRun(),
                tuition.getAcademicPeriod().getAcademicPeriodId(),
                tuition.getAcademicPeriod().getAcademicYear(),
                tuition.getAcademicPeriod().getAcademicSemester(),
                tuition.getTuitionDate()
        );
    }

    public List<TuitionResponseDTO> toResponseList(List<TuitionEntity> tuitions){
        return tuitions.stream().map(this::toResponse).toList();
    }
}

package SIGA_IPT.backend.mappers;

import SIGA_IPT.backend.dtos.TeacherDTOs.TeacherRequestDTO;
import SIGA_IPT.backend.dtos.TeacherDTOs.TeacherResponseDTO;
import SIGA_IPT.backend.entities.TeacherEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TeacherMapper {

    public TeacherEntity toEntity(TeacherRequestDTO dto){
        TeacherEntity teacher = new TeacherEntity();
        updateEntity(teacher, dto);
        return teacher;
    }

    public void updateEntity(TeacherEntity teacher, TeacherRequestDTO dto){
        teacher.setTeacherRun(dto.teacherRun());
        teacher.setTeacherFullname(dto.teacherFullname());
        teacher.setTeacherEmail(dto.teacherEmail());
        teacher.setTeacherProtitle(dto.teacherProtitle());
        teacher.setTeacherDegree(dto.teacherDegree());
    }

    public TeacherResponseDTO toResponse(TeacherEntity teacher){
        return new TeacherResponseDTO(
                teacher.getTeacherId(),
                teacher.getTeacherRun(),
                teacher.getTeacherFullname(),
                teacher.getTeacherEmail(),
                teacher.getTeacherProtitle(),
                teacher.getTeacherDegree(),
                teacher.getTeacherStatus()
        );
    }

    public List<TeacherResponseDTO> toResponseList(List<TeacherEntity> teachers){
        return teachers.stream().map(this::toResponse).toList();
    }
}

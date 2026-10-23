package SIGA_IPT.backend.mappers;

import SIGA_IPT.backend.dtos.StudentDTOs.StudentRequestDTO;
import SIGA_IPT.backend.dtos.StudentDTOs.StudentResponseDTO;
import SIGA_IPT.backend.dtos.StudentDTOs.StudentUpdateRequestDTO;
import SIGA_IPT.backend.entities.CareerEntity;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudentMapper {

    public StudentEntity toEntity(StudentRequestDTO dto, CareerEntity career, StudyplanEntity studyplan){
        StudentEntity student = new StudentEntity();
        student.setStudentRun(dto.studentRun());
        student.setStudentFullname(dto.studentFullname());
        student.setStudentEmail(dto.studentEmail());
        student.setCareer(career);
        student.setStudyplan(studyplan);
        return student;
    }

    public void updateEntity(StudentEntity student, StudentUpdateRequestDTO dto){
        student.setStudentFullname(dto.studentFullname());
        student.setStudentEmail(dto.studentEmail());
    }

    public StudentResponseDTO toResponse(StudentEntity student){
        return new StudentResponseDTO(
                student.getStudentId(),
                student.getStudentRun(),
                student.getStudentFullname(),
                student.getStudentEmail(),
                student.getCareer().getCareerCode(),
                student.getStudyplan().getStudyplanIdentifier(),
                student.getStudentStatus()
        );
    }

    public List<StudentResponseDTO> toResponseList(List<StudentEntity> students){
        return students.stream().map(this::toResponse).toList();
    }
}

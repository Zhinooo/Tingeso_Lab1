package SIGA_IPT.backend.mappers;

import SIGA_IPT.backend.dtos.EnrollmentDTOs.EnrollmentResponseDTO;
import SIGA_IPT.backend.entities.EnrollmentEntity;
import SIGA_IPT.backend.entities.SectionEntity;
import SIGA_IPT.backend.entities.StudentEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EnrollmentMapper {

    public EnrollmentEntity toEntity(StudentEntity student, SectionEntity section){
        EnrollmentEntity enrollment = new EnrollmentEntity();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollment.setCourse(section.getCourse());
        enrollment.setAcademicPeriod(section.getAcademicPeriod());
        return enrollment;
    }

    public EnrollmentResponseDTO toResponse(EnrollmentEntity enrollment){
        return new EnrollmentResponseDTO(
                enrollment.getEnrollmentId(),
                enrollment.getStudent().getStudentId(),
                enrollment.getStudent().getStudentRun(),
                enrollment.getCourse().getCourseId(),
                enrollment.getCourse().getCourseCode(),
                enrollment.getCourse().getCourseName(),
                enrollment.getSection().getSectionId(),
                enrollment.getAcademicPeriod().getAcademicPeriodId(),
                enrollment.getAcademicPeriod().getAcademicYear(),
                enrollment.getAcademicPeriod().getAcademicSemester(),
                enrollment.getEnrollmentDate(),
                enrollment.getFinalGrade()
        );
    }

    public List<EnrollmentResponseDTO> toResponseList(List<EnrollmentEntity> enrollments){
        return enrollments.stream().map(this::toResponse).toList();
    }
}

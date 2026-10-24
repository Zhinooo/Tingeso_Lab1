package SIGA_IPT.backend.mappers;

import SIGA_IPT.backend.dtos.EnrollmentDTOs.EnrollmentResponseDTO;
import SIGA_IPT.backend.dtos.EnrollmentDTOs.AvailableCourseDTO;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.EnrollmentEntity;
import SIGA_IPT.backend.entities.SectionEntity;
import SIGA_IPT.backend.entities.StudentEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EnrollmentMapper {

    private EnrollmentEntity.EnrollmentResult calculateResult(Double finalGrade){
        if (finalGrade == null){
            return null;
        }
        return finalGrade >= EnrollmentEntity.PASSING_GRADE
                ? EnrollmentEntity.EnrollmentResult.APPROVED
                : EnrollmentEntity.EnrollmentResult.FAILED;
    }

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
                enrollment.getFinalGrade(),
                calculateResult(enrollment.getFinalGrade())
        );
    }

    public List<EnrollmentResponseDTO> toResponseList(List<EnrollmentEntity> enrollments){
        return enrollments.stream().map(this::toResponse).toList();
    }

    public AvailableCourseDTO toAvailableCourse(CourseEntity course, SectionEntity section, long enrolledCount){
        return new AvailableCourseDTO(
                course.getCourseId(),
                course.getCourseCode(),
                course.getCourseName(),
                course.getSemester(),
                section.getSectionId(),
                section.getTeacher().getTeacherFullname(),
                section.getScheduleBlocks().stream().map(SectionEntity.ScheduleBlock::getBlockCode).toList(),
                section.getSectionCapacity(),
                Math.max(0, section.getSectionCapacity() - enrolledCount)
        );
    }
}

package SIGA_IPT.backend.mappers;

import SIGA_IPT.backend.dtos.CourseDTOs.CourseRequestDTO;
import SIGA_IPT.backend.dtos.CourseDTOs.CourseResponseDTO;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CourseMapper {

    public CourseEntity toEntity(CourseRequestDTO dto, StudyplanEntity studyplan, List<CourseEntity> prerequisites){
        CourseEntity course = new CourseEntity();
        course.setStudyplan(studyplan);
        updateEntity(course, dto, prerequisites);
        return course;
    }

    public void updateEntity(CourseEntity course, CourseRequestDTO dto, List<CourseEntity> prerequisites){
        course.setCourseCode(dto.courseCode());
        course.setCourseName(dto.courseName());
        course.setSemester(dto.semester());
        course.setTheoryHours(dto.theoryHours());
        course.setExerciseHours(dto.exerciseHours());
        course.setLabHours(dto.labHours());
        course.setSct(dto.sct());
        course.setPrerequisites(new ArrayList<>(prerequisites));
    }

    public CourseResponseDTO toResponse(CourseEntity course){
        return new CourseResponseDTO(
                course.getCourseId(),
                course.getCourseCode(),
                course.getCourseName(),
                course.getSemester(),
                course.getTheoryHours(),
                course.getExerciseHours(),
                course.getLabHours(),
                course.getSct(),
                course.getStudyplan().getStudyplanId(),
                course.getStudyplan().getStudyplanIdentifier(),
                course.getPrerequisites().stream().map(CourseEntity::getCourseCode).toList()
        );
    }

    public List<CourseResponseDTO> toResponseList(List<CourseEntity> courses){
        return courses.stream().map(this::toResponse).toList();
    }
}

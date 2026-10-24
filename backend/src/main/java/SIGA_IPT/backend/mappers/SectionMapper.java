package SIGA_IPT.backend.mappers;

import SIGA_IPT.backend.dtos.SectionDTOs.ScheduleBlockDTO;
import SIGA_IPT.backend.dtos.SectionDTOs.SectionRequestDTO;
import SIGA_IPT.backend.dtos.SectionDTOs.SectionResponseDTO;
import SIGA_IPT.backend.dtos.SectionDTOs.SectionUpdateRequestDTO;
import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.SectionEntity;
import SIGA_IPT.backend.entities.TeacherEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SectionMapper {

    private List<SectionEntity.ScheduleBlock> toBlocks(List<ScheduleBlockDTO> blocks){
        return blocks.stream()
                .map(block -> new SectionEntity.ScheduleBlock(block.scheduleDay(), block.module()))
                .toList();
    }

    public SectionEntity toEntity(SectionRequestDTO dto, CourseEntity course, AcademicPeriodEntity academicPeriod, TeacherEntity teacher){
        SectionEntity section = new SectionEntity();
        section.setCourse(course);
        section.setAcademicPeriod(academicPeriod);
        section.setTeacher(teacher);
        section.setSectionCapacity(dto.sectionCapacity());
        section.getScheduleBlocks().addAll(toBlocks(dto.scheduleBlocks()));
        return section;
    }

    public void updateEntity(SectionEntity section, SectionUpdateRequestDTO dto, TeacherEntity teacher){
        section.setTeacher(teacher);
        section.setSectionCapacity(dto.sectionCapacity());
        section.getScheduleBlocks().clear();
        section.getScheduleBlocks().addAll(toBlocks(dto.scheduleBlocks()));
    }

    public SectionResponseDTO toResponse(SectionEntity section){
        return new SectionResponseDTO(
                section.getSectionId(),
                section.getCourse().getCourseId(),
                section.getCourse().getCourseCode(),
                section.getCourse().getCourseName(),
                section.getAcademicPeriod().getAcademicPeriodId(),
                section.getAcademicPeriod().getAcademicYear(),
                section.getAcademicPeriod().getAcademicSemester(),
                section.getTeacher().getTeacherId(),
                section.getTeacher().getTeacherFullname(),
                section.getSectionCapacity(),
                section.getScheduleBlocks().stream().map(SectionEntity.ScheduleBlock::getBlockCode).toList()
        );
    }

    public List<SectionResponseDTO> toResponseList(List<SectionEntity> sections){
        return sections.stream().map(this::toResponse).toList();
    }
}

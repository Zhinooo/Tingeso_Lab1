package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.SectionDTOs.ScheduleBlockDTO;
import SIGA_IPT.backend.dtos.SectionDTOs.SectionRequestDTO;
import SIGA_IPT.backend.dtos.SectionDTOs.SectionResponseDTO;
import SIGA_IPT.backend.dtos.SectionDTOs.SectionUpdateRequestDTO;
import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.SectionEntity;
import SIGA_IPT.backend.entities.TeacherEntity;
import SIGA_IPT.backend.mappers.SectionMapper;
import SIGA_IPT.backend.repositories.AcademicPeriodRepository;
import SIGA_IPT.backend.repositories.CourseRepository;
import SIGA_IPT.backend.repositories.EnrollmentRepository;
import SIGA_IPT.backend.repositories.SectionRepository;
import SIGA_IPT.backend.repositories.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class SectionService {

    private static final int MIN_CAPACITY = 1;
    private static final int MAX_CAPACITY = 60;
    private static final int MIN_MODULE = 1;
    private static final int MAX_MODULE = 6;
    private static final int HOURS_PER_BLOCK = 2;

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final TeacherRepository teacherRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SectionMapper sectionMapper;

    public SectionService(SectionRepository sectionRepository,
                          CourseRepository courseRepository,
                          AcademicPeriodRepository academicPeriodRepository,
                          TeacherRepository teacherRepository,
                          EnrollmentRepository enrollmentRepository,
                          SectionMapper sectionMapper){
        this.sectionRepository = sectionRepository;
        this.courseRepository = courseRepository;
        this.academicPeriodRepository = academicPeriodRepository;
        this.teacherRepository = teacherRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.sectionMapper = sectionMapper;
    }

    private void validateFields(SectionRequestDTO dto){
        if (dto.courseId() == null){
            throw new IllegalArgumentException("The course is required.");
        }
        if (dto.academicPeriodId() == null){
            throw new IllegalArgumentException("The academic period is required.");
        }
        if (dto.teacherId() == null){
            throw new IllegalArgumentException("The teacher is required.");
        }
        validateCapacityAndBlocks(dto.sectionCapacity(), dto.scheduleBlocks());
    }

    private void validateUpdateFields(SectionUpdateRequestDTO dto){
        if (dto.teacherId() == null){
            throw new IllegalArgumentException("The teacher is required.");
        }
        validateCapacityAndBlocks(dto.sectionCapacity(), dto.scheduleBlocks());
    }

    private void validateCapacityAndBlocks(Integer capacity, List<ScheduleBlockDTO> blocks){
        if (capacity == null){
            throw new IllegalArgumentException("The section capacity is required.");
        }
        if (capacity < MIN_CAPACITY || capacity > MAX_CAPACITY){
            throw new IllegalArgumentException("The section capacity must be between " + MIN_CAPACITY + " and " + MAX_CAPACITY + ".");
        }
        if (blocks == null || blocks.isEmpty()){
            throw new IllegalArgumentException("The section must have at least one schedule block.");
        }

        Set<String> codes = new HashSet<>();
        for (ScheduleBlockDTO block : blocks){
            if (block == null || block.scheduleDay() == null){
                throw new IllegalArgumentException("Every schedule block requires a valid day (L, M, W, J or V).");
            }
            if (block.module() == null || block.module() < MIN_MODULE || block.module() > MAX_MODULE){
                throw new IllegalArgumentException("Every schedule block requires a module between " + MIN_MODULE + " and " + MAX_MODULE + ".");
            }
            if (!codes.add(blockCode(block))){
                throw new IllegalArgumentException("Duplicated schedule block: " + blockCode(block));
            }
        }
    }

    private void validateBlockCount(CourseEntity course, List<ScheduleBlockDTO> blocks){
        int weeklyHours = course.getTheoryHours() + course.getExerciseHours() + course.getLabHours();
        if (blocks.size() * HOURS_PER_BLOCK != weeklyHours){
            throw new IllegalArgumentException("The number of schedule blocks must match the weekly TEL hours of the course: "
                    + weeklyHours + " hours require " + weeklyHours / (double) HOURS_PER_BLOCK + " blocks.");
        }
    }

    private void validateTeacherSchedule(TeacherEntity teacher, AcademicPeriodEntity academicPeriod, List<ScheduleBlockDTO> blocks, Long sectionIdToIgnore){
        Set<String> requested = blocks.stream().map(this::blockCode).collect(Collectors.toSet());

        for (SectionEntity other : sectionRepository.findByTeacherAndAcademicPeriod(teacher, academicPeriod)){
            if (other.getSectionId().equals(sectionIdToIgnore)){
                continue;
            }
            for (SectionEntity.ScheduleBlock block : other.getScheduleBlocks()){
                if (requested.contains(block.getBlockCode())){
                    throw new IllegalStateException("Schedule conflict: the teacher already has a section in block " + block.getBlockCode() + ".");
                }
            }
        }
    }

    private String blockCode(ScheduleBlockDTO block){
        return block.scheduleDay().name() + block.module();
    }

    private void requireOpenPeriod(AcademicPeriodEntity academicPeriod){
        if (academicPeriod.getAcademicPeriodStatus() != AcademicPeriodEntity.AcademicPeriodStatus.OPEN){
            throw new IllegalStateException("The academic period is closed.");
        }
    }

    private SectionEntity findSectionOrThrow(Long id){
        return sectionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Section not found: " + id));
    }

    private CourseEntity findCourseOrThrow(Long id){
        return courseRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Course not found: " + id));
    }

    private AcademicPeriodEntity findAcademicPeriodOrThrow(Long id){
        return academicPeriodRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Academic period not found: " + id));
    }

    private TeacherEntity findTeacherOrThrow(Long id){
        return teacherRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Teacher not found: " + id));
    }

    private void requireActiveTeacher(TeacherEntity teacher){
        if (teacher.getTeacherStatus() != TeacherEntity.TeacherStatus.ACTIVE){
            throw new IllegalStateException("The teacher must be active to be assigned to a section.");
        }
    }

    public SectionResponseDTO create(SectionRequestDTO dto){
        validateFields(dto);

        CourseEntity course = findCourseOrThrow(dto.courseId());
        AcademicPeriodEntity academicPeriod = findAcademicPeriodOrThrow(dto.academicPeriodId());
        TeacherEntity teacher = findTeacherOrThrow(dto.teacherId());

        requireOpenPeriod(academicPeriod);
        requireActiveTeacher(teacher);

        if (sectionRepository.existsByCourseAndAcademicPeriod(course, academicPeriod)){
            throw new IllegalArgumentException("The course already has a section in this academic period.");
        }

        validateBlockCount(course, dto.scheduleBlocks());
        validateTeacherSchedule(teacher, academicPeriod, dto.scheduleBlocks(), null);

        SectionEntity section = sectionMapper.toEntity(dto, course, academicPeriod, teacher);

        return sectionMapper.toResponse(sectionRepository.save(section));
    }

    @Transactional(readOnly = true)
    public List<SectionResponseDTO> getAll(){
        return sectionMapper.toResponseList(sectionRepository.findAll());
    }

    @Transactional(readOnly = true)
    public SectionResponseDTO getById(Long id){
        return sectionMapper.toResponse(findSectionOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<SectionResponseDTO> getByAcademicPeriod(Long academicPeriodId){
        AcademicPeriodEntity academicPeriod = findAcademicPeriodOrThrow(academicPeriodId);
        return sectionMapper.toResponseList(sectionRepository.findByAcademicPeriod(academicPeriod));
    }

    public SectionResponseDTO update(Long id, SectionUpdateRequestDTO dto){
        validateUpdateFields(dto);
        SectionEntity section = findSectionOrThrow(id);

        requireOpenPeriod(section.getAcademicPeriod());

        TeacherEntity teacher = findTeacherOrThrow(dto.teacherId());
        boolean teacherChanged = !section.getTeacher().getTeacherId().equals(teacher.getTeacherId());
        if (teacherChanged){
            requireActiveTeacher(teacher);
        }

        validateBlockCount(section.getCourse(), dto.scheduleBlocks());

        Set<String> currentBlocks = section.getScheduleBlocks().stream()
                .map(SectionEntity.ScheduleBlock::getBlockCode)
                .collect(Collectors.toSet());
        Set<String> requestedBlocks = dto.scheduleBlocks().stream()
                .map(this::blockCode)
                .collect(Collectors.toSet());
        boolean scheduleChanged = !currentBlocks.equals(requestedBlocks);

        long enrolled = enrollmentRepository.countBySection(section);
        if (enrolled > 0 && (teacherChanged || scheduleChanged)){
            throw new IllegalStateException("Cannot change the teacher or schedule: the section has enrolled students.");
        }
        if (dto.sectionCapacity() < enrolled){
            throw new IllegalStateException("The capacity cannot be lower than the number of enrolled students: " + enrolled);
        }

        validateTeacherSchedule(teacher, section.getAcademicPeriod(), dto.scheduleBlocks(), section.getSectionId());

        sectionMapper.updateEntity(section, dto, teacher);

        return sectionMapper.toResponse(sectionRepository.save(section));
    }

    public void delete(Long id){
        SectionEntity section = findSectionOrThrow(id);

        requireOpenPeriod(section.getAcademicPeriod());

        if (enrollmentRepository.existsBySection(section)){
            throw new IllegalStateException("Cannot delete: the section has enrolled students.");
        }

        sectionRepository.delete(section);
    }
}

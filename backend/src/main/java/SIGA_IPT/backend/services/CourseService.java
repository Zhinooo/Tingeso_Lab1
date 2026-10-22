package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.CourseDTOs.CourseRequestDTO;
import SIGA_IPT.backend.dtos.CourseDTOs.CourseResponseDTO;
import SIGA_IPT.backend.entities.CareerEntity;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.StudyplanEntity;
import SIGA_IPT.backend.mappers.CourseMapper;
import SIGA_IPT.backend.repositories.CourseRepository;
import SIGA_IPT.backend.repositories.EnrollmentRepository;
import SIGA_IPT.backend.repositories.SectionRepository;
import SIGA_IPT.backend.repositories.StudyplanRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CourseService {

    private static final int MAX_PREREQUISITES = 3;
    private static final int MAX_SCT = 7;
    private static final int MAX_WEEKLY_HOURS_EXCLUSIVE = 8;

    private final CourseRepository courseRepository;
    private final StudyplanRepository studyplanRepository;
    private final SectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseMapper courseMapper;

    public CourseService(CourseRepository courseRepository,
                         StudyplanRepository studyplanRepository,
                         SectionRepository sectionRepository,
                         EnrollmentRepository enrollmentRepository,
                         CourseMapper courseMapper){
        this.courseRepository = courseRepository;
        this.studyplanRepository = studyplanRepository;
        this.sectionRepository = sectionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseMapper = courseMapper;
    }

    private void validateFields(CourseRequestDTO dto){
        if (dto.courseCode() == null || dto.courseCode().isBlank()){
            throw new IllegalArgumentException("The course code is required.");
        }
        if (dto.courseName() == null || dto.courseName().isBlank()){
            throw new IllegalArgumentException("The course name is required.");
        }
        if (dto.semester() == null){
            throw new IllegalArgumentException("The semester is required.");
        }
        if (dto.semester() < 1 || dto.semester() > CareerEntity.DURATION_CAREER){
            throw new IllegalArgumentException("The semester must be between 1 and " + CareerEntity.DURATION_CAREER + ".");
        }
        if (dto.theoryHours() == null || dto.exerciseHours() == null || dto.labHours() == null){
            throw new IllegalArgumentException("Theory, exercise and lab hours are required.");
        }
        if (dto.theoryHours() < 0 || dto.exerciseHours() < 0 || dto.labHours() < 0){
            throw new IllegalArgumentException("Weekly hours cannot be negative.");
        }
        if (dto.theoryHours() + dto.exerciseHours() + dto.labHours() >= MAX_WEEKLY_HOURS_EXCLUSIVE){
            throw new IllegalArgumentException("The total weekly hours must be less than " + MAX_WEEKLY_HOURS_EXCLUSIVE + ".");
        }
        if (dto.sct() == null){
            throw new IllegalArgumentException("The SCT is required.");
        }
        if (dto.sct() <= 0 || dto.sct() > MAX_SCT){
            throw new IllegalArgumentException("The SCT must be greater than 0 and at most " + MAX_SCT + ".");
        }
    }

    private CourseEntity findCourseOrThrow(Long id){
        return courseRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Course not found: " + id));
    }

    private StudyplanEntity findStudyplanOrThrow(Long id){
        return studyplanRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Study plan not found: " + id));
    }

    private List<CourseEntity> resolvePrerequisites(CourseRequestDTO dto, StudyplanEntity studyplan){
        List<String> codes = dto.prerequisiteCodes() == null ? List.of() : dto.prerequisiteCodes();

        if (codes.size() > MAX_PREREQUISITES){
            throw new IllegalArgumentException("A course can have at most " + MAX_PREREQUISITES + " prerequisites.");
        }
        if (codes.stream().distinct().count() != codes.size()){
            throw new IllegalArgumentException("The prerequisites cannot contain duplicates.");
        }

        List<CourseEntity> prerequisites = new ArrayList<>();
        for (String code : codes){
            CourseEntity prerequisite = courseRepository.findByCourseCodeAndStudyplan(code, studyplan)
                    .orElseThrow(() -> new EntityNotFoundException("Prerequisite not found in this study plan: " + code));
            if (prerequisite.getSemester() >= dto.semester()){
                throw new IllegalArgumentException("The prerequisite " + code + " must belong to an earlier semester.");
            }
            prerequisites.add(prerequisite);
        }
        return prerequisites;
    }

    public CourseResponseDTO create(CourseRequestDTO dto){
        validateFields(dto);
        if (dto.studyplanId() == null){
            throw new IllegalArgumentException("The study plan is required.");
        }

        StudyplanEntity studyplan = findStudyplanOrThrow(dto.studyplanId());

        if (courseRepository.existsByCourseCodeAndStudyplan(dto.courseCode(), studyplan)){
            throw new IllegalArgumentException("This study plan already has a course with code: " + dto.courseCode());
        }

        List<CourseEntity> prerequisites = resolvePrerequisites(dto, studyplan);
        CourseEntity course = courseMapper.toEntity(dto, studyplan, prerequisites);

        return courseMapper.toResponse(courseRepository.save(course));
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDTO> getAll(){
        return courseMapper.toResponseList(courseRepository.findAll());
    }

    @Transactional(readOnly = true)
    public CourseResponseDTO getById(Long id){
        return courseMapper.toResponse(findCourseOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<CourseResponseDTO> getByStudyplan(Long studyplanId){
        StudyplanEntity studyplan = findStudyplanOrThrow(studyplanId);
        return courseMapper.toResponseList(courseRepository.findByStudyplan(studyplan));
    }

    public CourseResponseDTO update(Long id, CourseRequestDTO dto){
        validateFields(dto);
        CourseEntity course = findCourseOrThrow(id);
        StudyplanEntity studyplan = course.getStudyplan();

        if (dto.studyplanId() != null && !dto.studyplanId().equals(studyplan.getStudyplanId())){
            throw new IllegalArgumentException("A course cannot change its study plan.");
        }
        if (enrollmentRepository.existsByCourse(course)){
            throw new IllegalStateException("Cannot modify: the course is part of the academic history.");
        }

        boolean codeChanged = !course.getCourseCode().equals(dto.courseCode());
        if (codeChanged && courseRepository.existsByCourseCodeAndStudyplan(dto.courseCode(), studyplan)){
            throw new IllegalArgumentException("This study plan already has a course with code: " + dto.courseCode());
        }

        boolean semesterChanged = course.getSemester() != dto.semester();
        if (semesterChanged && courseRepository.isPrerequisiteOfAnyCourse(course)){
            throw new IllegalStateException("Cannot change the semester: the course is a prerequisite of other courses.");
        }

        List<CourseEntity> prerequisites = resolvePrerequisites(dto, studyplan);
        courseMapper.updateEntity(course, dto, prerequisites);

        return courseMapper.toResponse(courseRepository.save(course));
    }

    public void delete(Long id){
        CourseEntity course = findCourseOrThrow(id);

        if (enrollmentRepository.existsByCourse(course)){
            throw new IllegalStateException("Cannot delete: the course is part of the academic history.");
        }
        if (sectionRepository.existsByCourse(course)){
            throw new IllegalStateException("Cannot delete: the course has associated sections.");
        }
        if (courseRepository.isPrerequisiteOfAnyCourse(course)){
            throw new IllegalStateException("Cannot delete: the course is a prerequisite of other courses.");
        }

        courseRepository.delete(course);
    }
}

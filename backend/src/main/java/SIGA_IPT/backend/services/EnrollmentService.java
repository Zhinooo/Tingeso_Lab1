package SIGA_IPT.backend.services;

import SIGA_IPT.backend.dtos.EnrollmentDTOs.EnrollmentRequestDTO;
import SIGA_IPT.backend.dtos.EnrollmentDTOs.EnrollmentResponseDTO;
import SIGA_IPT.backend.dtos.EnrollmentDTOs.AvailableCourseDTO;
import SIGA_IPT.backend.entities.AcademicPeriodEntity;
import SIGA_IPT.backend.entities.CourseEntity;
import SIGA_IPT.backend.entities.EnrollmentEntity;
import SIGA_IPT.backend.entities.SectionEntity;
import SIGA_IPT.backend.entities.StudentEntity;
import SIGA_IPT.backend.mappers.EnrollmentMapper;
import SIGA_IPT.backend.repositories.AcademicPeriodRepository;
import SIGA_IPT.backend.repositories.CourseRepository;
import SIGA_IPT.backend.repositories.EnrollmentRepository;
import SIGA_IPT.backend.repositories.SectionRepository;
import SIGA_IPT.backend.repositories.StudentRepository;
import SIGA_IPT.backend.repositories.TuitionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class EnrollmentService {

    private static final int MIN_COURSES = 3;
    private static final int MAX_COURSES = 6;

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final TuitionRepository tuitionRepository;
    private final CourseRepository courseRepository;
    private final SectionRepository sectionRepository;
    private final EnrollmentMapper enrollmentMapper;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             StudentRepository studentRepository,
                             AcademicPeriodRepository academicPeriodRepository,
                             TuitionRepository tuitionRepository,
                             CourseRepository courseRepository,
                             SectionRepository sectionRepository,
                             EnrollmentMapper enrollmentMapper){
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.academicPeriodRepository = academicPeriodRepository;
        this.tuitionRepository = tuitionRepository;
        this.courseRepository = courseRepository;
        this.sectionRepository = sectionRepository;
        this.enrollmentMapper = enrollmentMapper;
    }

    private void validateFields(EnrollmentRequestDTO dto){
        if (dto.studentId() == null){
            throw new IllegalArgumentException("The student is required.");
        }
        if (dto.academicPeriodId() == null){
            throw new IllegalArgumentException("The academic period is required.");
        }
        if (dto.courseIds() == null || dto.courseIds().isEmpty()){
            throw new IllegalArgumentException("At least one course is required.");
        }
        if (dto.courseIds().contains(null)){
            throw new IllegalArgumentException("Every course id is required.");
        }
        if (new HashSet<>(dto.courseIds()).size() != dto.courseIds().size()){
            throw new IllegalArgumentException("The same course cannot be selected more than once.");
        }
    }

    private EnrollmentEntity findEnrollmentOrThrow(Long id){
        return enrollmentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Enrollment not found: " + id));
    }

    private StudentEntity findStudentOrThrow(Long id){
        return studentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Student not found: " + id));
    }

    private AcademicPeriodEntity findAcademicPeriodOrThrow(Long id){
        return academicPeriodRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Academic period not found: " + id));
    }

    private SectionEntity findSectionOrThrow(Long id){
        return sectionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Section not found: " + id));
    }

    private void requireOpenPeriod(AcademicPeriodEntity academicPeriod){
        if (academicPeriod.getAcademicPeriodStatus() != AcademicPeriodEntity.AcademicPeriodStatus.OPEN){
            throw new IllegalStateException("The academic period is closed.");
        }
    }

    private Set<Long> findApprovedCourseIds(StudentEntity student){
        return enrollmentRepository
                .findByStudentAndAcademicPeriod_AcademicPeriodStatus(student, AcademicPeriodEntity.AcademicPeriodStatus.CLOSED)
                .stream()
                .filter(enrollment -> enrollment.getFinalGrade() != null && enrollment.getFinalGrade() >= EnrollmentEntity.PASSING_GRADE)
                .map(enrollment -> enrollment.getCourse().getCourseId())
                .collect(Collectors.toSet());
    }

    private Map<Long, SectionEntity> findSectionsByCourse(AcademicPeriodEntity academicPeriod){
        return sectionRepository.findByAcademicPeriod(academicPeriod).stream()
                .collect(Collectors.toMap(section -> section.getCourse().getCourseId(), section -> section));
    }

    private boolean isAvailable(CourseEntity course, Set<Long> approvedCourseIds, Map<Long, SectionEntity> sectionsByCourse){
        return !approvedCourseIds.contains(course.getCourseId())
                && sectionsByCourse.containsKey(course.getCourseId())
                && course.getPrerequisites().stream().allMatch(prerequisite -> approvedCourseIds.contains(prerequisite.getCourseId()));
    }

    private void validateCourseCount(int totalCourses, int availableCourses){
        if (totalCourses > MAX_COURSES){
            throw new IllegalStateException("A student cannot enroll more than " + MAX_COURSES + " courses per academic period.");
        }
        int minimum = Math.min(MIN_COURSES, availableCourses);
        if (totalCourses < minimum){
            throw new IllegalStateException("A student must enroll at least " + minimum + " courses in this academic period.");
        }
    }

    private void validateSemesterOrder(List<CourseEntity> selected, List<CourseEntity> available, Set<Long> coveredCourseIds){
        for (CourseEntity course : selected){
            for (CourseEntity other : available){
                if (other.getSemester() < course.getSemester() && !coveredCourseIds.contains(other.getCourseId())){
                    throw new IllegalStateException("Cannot enroll " + course.getCourseCode()
                            + ": the student still has the available lower-semester course " + other.getCourseCode() + ".");
                }
            }
        }
    }

    private void validateScheduleConflicts(List<EnrollmentEntity> current, List<SectionEntity> newSections){
        Map<String, String> occupiedBlocks = new HashMap<>();

        for (EnrollmentEntity enrollment : current){
            for (SectionEntity.ScheduleBlock block : enrollment.getSection().getScheduleBlocks()){
                occupiedBlocks.put(block.getBlockCode(), enrollment.getCourse().getCourseCode());
            }
        }

        for (SectionEntity section : newSections){
            for (SectionEntity.ScheduleBlock block : section.getScheduleBlocks()){
                String conflictingCourse = occupiedBlocks.put(block.getBlockCode(), section.getCourse().getCourseCode());
                if (conflictingCourse != null){
                    throw new IllegalStateException("Schedule conflict in block " + block.getBlockCode() + " between "
                            + conflictingCourse + " and " + section.getCourse().getCourseCode() + ".");
                }
            }
        }
    }

    private void lockSections(List<Long> courseIds, Map<Long, SectionEntity> sectionsByCourse){
        courseIds.stream()
                .map(sectionsByCourse::get)
                .filter(Objects::nonNull)
                .map(SectionEntity::getSectionId)
                .sorted()
                .forEach(sectionRepository::findForUpdateBySectionId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<EnrollmentResponseDTO> create(EnrollmentRequestDTO dto){
        validateFields(dto);

        StudentEntity student = findStudentOrThrow(dto.studentId());
        AcademicPeriodEntity academicPeriod = findAcademicPeriodOrThrow(dto.academicPeriodId());

        requireOpenPeriod(academicPeriod);
        if (student.getStudentStatus() != StudentEntity.StudentStatus.REGULAR){
            throw new IllegalStateException("Only regular students can enroll courses.");
        }
        if (!tuitionRepository.existsByStudentAndAcademicPeriod(student, academicPeriod)){
            throw new IllegalStateException("The student has no tuition in this academic period.");
        }

        Map<Long, CourseEntity> planCourses = courseRepository.findByStudyplan(student.getStudyplan()).stream()
                .collect(Collectors.toMap(CourseEntity::getCourseId, course -> course));
        Map<Long, SectionEntity> sectionsByCourse = findSectionsByCourse(academicPeriod);
        lockSections(dto.courseIds(), sectionsByCourse);
        List<EnrollmentEntity> current = enrollmentRepository.findByStudentAndAcademicPeriod(student, academicPeriod);
        Set<Long> currentCourseIds = current.stream()
                .map(enrollment -> enrollment.getCourse().getCourseId())
                .collect(Collectors.toSet());
        Set<Long> approvedCourseIds = findApprovedCourseIds(student);

        List<CourseEntity> selected = new ArrayList<>();
        List<SectionEntity> newSections = new ArrayList<>();

        for (Long courseId : dto.courseIds()){
            CourseEntity course = planCourses.get(courseId);
            if (course == null){
                throw new IllegalArgumentException("The course " + courseId + " does not belong to the student's study plan.");
            }
            if (currentCourseIds.contains(courseId)){
                throw new IllegalArgumentException("The student is already enrolled in " + course.getCourseCode() + " in this academic period.");
            }
            if (approvedCourseIds.contains(courseId)){
                throw new IllegalStateException("The course " + course.getCourseCode() + " is already approved.");
            }

            SectionEntity section = sectionsByCourse.get(courseId);
            if (section == null){
                throw new IllegalStateException("The course " + course.getCourseCode() + " is not offered in this academic period.");
            }
            for (CourseEntity prerequisite : course.getPrerequisites()){
                if (!approvedCourseIds.contains(prerequisite.getCourseId())){
                    throw new IllegalStateException("Cannot enroll " + course.getCourseCode()
                            + ": the prerequisite " + prerequisite.getCourseCode() + " is not approved.");
                }
            }
            if (enrollmentRepository.countBySection(section) >= section.getSectionCapacity()){
                throw new IllegalStateException("The section of " + course.getCourseCode() + " has no available capacity.");
            }

            selected.add(course);
            newSections.add(section);
        }

        List<CourseEntity> available = planCourses.values().stream()
                .filter(course -> isAvailable(course, approvedCourseIds, sectionsByCourse))
                .toList();

        Set<Long> coveredCourseIds = new HashSet<>(currentCourseIds);
        selected.forEach(course -> coveredCourseIds.add(course.getCourseId()));

        validateCourseCount(current.size() + selected.size(), available.size());
        validateSemesterOrder(selected, available, coveredCourseIds);
        validateScheduleConflicts(current, newSections);

        LocalDate today = LocalDate.now();
        List<EnrollmentEntity> enrollments = new ArrayList<>();
        for (SectionEntity section : newSections){
            EnrollmentEntity enrollment = enrollmentMapper.toEntity(student, section);
            enrollment.setEnrollmentDate(today);
            enrollments.add(enrollment);
        }

        return enrollmentMapper.toResponseList(enrollmentRepository.saveAll(enrollments));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getAll(){
        return enrollmentMapper.toResponseList(enrollmentRepository.findAll());
    }

    @Transactional(readOnly = true)
    public EnrollmentResponseDTO getById(Long id){
        return enrollmentMapper.toResponse(findEnrollmentOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getByStudent(Long studentId){
        StudentEntity student = findStudentOrThrow(studentId);
        return enrollmentMapper.toResponseList(enrollmentRepository.findByStudent(student));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getAcademicHistory(Long studentId){
        StudentEntity student = findStudentOrThrow(studentId);
        return enrollmentMapper.toResponseList(
                enrollmentRepository.findByStudentAndAcademicPeriod_AcademicPeriodStatus(student, AcademicPeriodEntity.AcademicPeriodStatus.CLOSED));
    }

    @Transactional(readOnly = true)
    public List<AvailableCourseDTO> getAvailableOffer(Long studentId, Long academicPeriodId){
        StudentEntity student = findStudentOrThrow(studentId);
        AcademicPeriodEntity academicPeriod = findAcademicPeriodOrThrow(academicPeriodId);

        Map<Long, SectionEntity> sectionsByCourse = findSectionsByCourse(academicPeriod);
        Set<Long> approvedCourseIds = findApprovedCourseIds(student);
        Set<Long> enrolledCourseIds = enrollmentRepository.findByStudentAndAcademicPeriod(student, academicPeriod).stream()
                .map(enrollment -> enrollment.getCourse().getCourseId())
                .collect(Collectors.toSet());

        return courseRepository.findByStudyplan(student.getStudyplan()).stream()
                .filter(course -> isAvailable(course, approvedCourseIds, sectionsByCourse))
                .filter(course -> !enrolledCourseIds.contains(course.getCourseId()))
                .sorted(Comparator.comparingInt(CourseEntity::getSemester).thenComparing(CourseEntity::getCourseCode))
                .map(course -> {
                    SectionEntity section = sectionsByCourse.get(course.getCourseId());
                    return enrollmentMapper.toAvailableCourse(course, section, enrollmentRepository.countBySection(section));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getByAcademicPeriod(Long academicPeriodId){
        AcademicPeriodEntity academicPeriod = findAcademicPeriodOrThrow(academicPeriodId);
        return enrollmentMapper.toResponseList(enrollmentRepository.findByAcademicPeriod(academicPeriod));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> getBySection(Long sectionId){
        SectionEntity section = findSectionOrThrow(sectionId);
        return enrollmentMapper.toResponseList(enrollmentRepository.findBySection(section));
    }

    public void cancel(Long id){
        EnrollmentEntity enrollment = findEnrollmentOrThrow(id);

        requireOpenPeriod(enrollment.getAcademicPeriod());

        if (enrollment.getFinalGrade() != null){
            throw new IllegalStateException("Cannot cancel: the enrollment already has a final grade.");
        }

        enrollmentRepository.delete(enrollment);
    }
}
